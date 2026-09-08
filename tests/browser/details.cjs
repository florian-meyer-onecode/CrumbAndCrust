const { chromium, expect, assert, path, baseURL, output, reveal, click, type, stored } = require('./helpers.cjs');
(async () => {
    const browser = await chromium.launch({ headless: true, ...(process.env.BROWSER_CHANNEL ? { channel: process.env.BROWSER_CHANNEL } : {}) });
    try {
        const context = await browser.newContext({ viewport: { width: 1280, height: 1000 } });
        const page = await context.newPage();
        const errors = [];
        page.on('pageerror', e => errors.push(e.message));
        await page.goto(baseURL, { waitUntil: 'networkidle' });
        const recipe = { id: 'details', name: 'Landbrot', description: 'Knuspriges Brot mit langer Teigruhe.', schemaVersion: 1,
            ingredients: [{ id: 'flour', name: 'Weizenmehl', grams: 500 }, { id: 'water', name: 'Wasser', grams: 350 }, { id: 'salt', name: 'Salz', grams: 10 }],
            steps: [{ id: 'mix', title: 'Teig mischen', instruction: 'Mehl und Wasser vermengen.\nDann das Salz unterkneten.', durationMinutes: 15 },
                { id: 'rest', title: 'Teigruhe', instruction: 'Abgedeckt im Kühlschrank ruhen lassen.', durationMinutes: 1500 },
                { id: 'bake', title: 'Backen', instruction: 'Im vorgeheizten Ofen backen.', durationMinutes: 45 },
                { id: 'done', title: 'Fertig', instruction: '', durationMinutes: 0 }] };
        const other = { ...recipe, id: 'other', name: 'Zweites Brot', ingredients: [{ id: 'other-flour', name: 'Roggenmehl', grams: 400 }] };
        await page.evaluate(async records => { for (const r of records) await CrumbRecipeStorage.add(JSON.stringify(r)); }, [recipe, other]);
        await page.reload({ waitUntil: 'networkidle' });
        await expect(page.getByRole('heading', { name: 'Landbrot', exact: true })).toBeVisible();
        const text = () => page.locator('body').ariaSnapshot();
        await expect.poll(text).toMatch( /26 Std\./);
        for (const s of recipe.steps) await expect(page.getByRole('heading', { name: s.title, exact: true })).toBeAttached();
        await expect.poll(text).toMatch( /Dauer: 0 Min\./);
        await expect.poll(text).toMatch( /Dann das Salz unterkneten/);
        await expect.poll(text).not.toMatch( /Beginn: /);
        const timeline = await text();
        const positions = recipe.steps.map(s => timeline.indexOf(`heading "${s.title}"`));
        assert.ok(positions.every((p, i) => p >= 0 && (i === 0 || p > positions[i - 1])));
        await page.screenshot({ path: path.join(output, 'details-desktop.png') });
        await click(page, '2× Ansatz');
        await expect.poll(text).toMatch( /1000 g/);
        await expect.poll(text).toMatch( /700 g/);
        await expect.poll(text).toMatch( /20 g/);
        await expect.poll(text).toMatch( /26 Std\./);
        await type(page, 'Gewünschtes Gesamtteiggewicht in g', '430,5');
        await expect.poll(text).toMatch( /250,29 g/);
        await expect.poll(text).toMatch( /175,2 g/);
        await expect.poll(text).toMatch( /5,01 g/);
        await type(page, 'Gewünschtes Gesamtteiggewicht in g', '0');
        await expect.poll(text).toMatch( /letzte gültige Menge: 430,5 g/);
        await expect.poll(text).toMatch( /250,29 g/);
        await click(page, '1× Ansatz');
        await expect.poll(text).toMatch( /500 g/);
        await expect.poll(text).not.toMatch( /letzte gültige Menge/);
        assert.deepEqual(await stored(page), [recipe, other]);
        console.log('PASS: ordered timeline, multiline instructions, zero/multi-day durations, proportional scaling, invalid-input recovery; stored recipe unchanged');
        await click(page, '2× Ansatz');
        await click(page, /^Zweites Brot/);
        await expect.poll(text).toMatch( /Angezeigte Gesamtmenge: 400 g/);
        await click(page, /^Landbrot/);
        await expect.poll(text).toMatch( /Angezeigte Gesamtmenge: 860 g/);
        await click(page, '2× Ansatz');
        await click(page, 'Rezept bearbeiten');
        const field = page.getByRole('textbox', { name: 'Zutat 1: Menge in g *', exact: true });
        await reveal(page, field);
        assert.equal(await field.innerText(), '500');
        await click(page, 'Abbrechen', true);
        await page.reload({ waitUntil: 'networkidle' });
        await expect.poll(text).toMatch( /Angezeigte Gesamtmenge: 860 g/);
        console.log('PASS: selection and reload reset scaling; editor still shows original amounts');
        await page.setViewportSize({ width: 390, height: 844 });
        await click(page, '2× Ansatz');
        await reveal(page, page.getByRole('textbox', { name: 'Gewünschtes Gesamtteiggewicht in g', exact: true }));
        await page.screenshot({ path: path.join(output, 'details-mobile-ingredients.png') });
        await reveal(page, page.getByRole('heading', { name: 'Teig mischen', exact: true }));
        await page.screenshot({ path: path.join(output, 'details-mobile-timeline.png') });
        for (const title of recipe.steps.map(s => s.title)) {
            const heading = page.getByRole('heading', { name: title, exact: true });
            await reveal(page, heading);
            const box = await heading.boundingBox();
            assert.ok(box.x >= 0 && box.x + box.width <= 390);
        }
        assert.deepEqual(await stored(page), [recipe, other]);
        assert.deepEqual(errors, []);
        console.log('PASS: narrow ingredient controls and entire timeline accessible; no runtime errors');
    } finally { await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
