const { chromium, expect, assert, fs, path, baseURL, output, reveal, click, type, stored } = require('./helpers.cjs');

async function readField(page, name) {
    const field = page.getByRole('textbox', { name, exact: true });
    await reveal(page, field);
    return field.innerText();
}

(async () => {
    const browser = await chromium.launch({ headless: true, ...(process.env.BROWSER_CHANNEL ? { channel: process.env.BROWSER_CHANNEL } : {}) });
    let page;
    try {
        const context = await browser.newContext({ viewport: { width: 1280, height: 1000 } });
        page = await context.newPage();
        const errors = [];
        page.on('pageerror', error => errors.push(error.message));
        await page.goto(baseURL, { waitUntil: 'networkidle' });
        const original = {
            id: 'existing-from-step-4', name: 'Zwiebelbrot', description: 'Mit Öl & Körnern.\nÜber Nacht.', schemaVersion: 1,
            ingredients: [{ id: 'flour', name: 'Mehl', grams: 500 }, { id: 'yeast', name: 'Hefe', grams: 0.5 }, { id: 'tiny', name: 'Gewürz', grams: 0.0000001 }],
            steps: [{ id: 'mix', title: 'Mischen', instruction: 'Alles vermengen.', durationMinutes: 15 }, { id: 'rest', title: 'Ruhen', instruction: 'Kalt stellen.', durationMinutes: 1500 }, { id: 'bake', title: 'Backen', instruction: '', durationMinutes: 40 }],
        };
        const other = { ...original, id: 'untouched', name: 'Ackerbrot', description: 'Dieses Rezept bleibt unverändert.' };
        await page.evaluate(async recipes => { for (const recipe of recipes) await CrumbRecipeStorage.add(JSON.stringify(recipe)); }, [original, other]);
        await page.reload({ waitUntil: 'networkidle' });
        await click(page, /^Zwiebelbrot/);
        await click(page, 'Rezept bearbeiten');
        await expect(page.getByRole('heading', { name: 'Rezept bearbeiten', exact: true })).toBeVisible();
        assert.equal(await readField(page, 'Rezeptname *'), original.name);
        assert.equal(await readField(page, 'Kurzbeschreibung'), original.description);
        assert.equal(await readField(page, 'Zutat 2: Menge in g *'), '0,5');
        assert.equal(await readField(page, 'Zutat 3: Menge in g *'), '0,0000001');
        assert.equal(await readField(page, 'Schritt 2: Stunden'), '25');
        assert.equal(await readField(page, 'Schritt 2: Minuten'), '0');
        await click(page, 'Änderungen speichern', true);
        assert.deepEqual((await stored(page)).find(r => r.id === original.id), original);
        console.log('PASS: step-4 records open without migration; unchanged editing preserves every value and ID');

        await click(page, 'Rezept bearbeiten');
        await type(page, 'Rezeptname *', 'Nur ein verworfener Entwurf');
        await type(page, 'Zutat 1: Menge in g *', '0');
        await click(page, 'Schritt 1 entfernen');
        await click(page, 'Abbrechen', true);
        assert.deepEqual((await stored(page)).find(r => r.id === original.id), original);
        await expect(page.getByRole('heading', { name: 'Zwiebelbrot', exact: true })).toBeVisible();
        await click(page, 'Rezept bearbeiten');
        assert.equal(await readField(page, 'Rezeptname *'), original.name);
        assert.equal(await readField(page, 'Zutat 1: Menge in g *'), '500');
        console.log('PASS: cancel discards edits and preserves the selected recipe');

        await type(page, 'Rezeptname *', 'Roggenbrot');
        await type(page, 'Kurzbeschreibung', 'Mehr Wasser, längere Ruhe.');
        await type(page, 'Zutat 1: Name *', 'Roggenmehl');
        await type(page, 'Zutat 1: Menge in g *', '600,25');
        await click(page, 'Zutat 3 entfernen');
        await click(page, /Zutat hinzufügen/);
        await type(page, 'Zutat 3: Name *', 'Wasser');
        await type(page, 'Zutat 3: Menge in g *', '400');
        await type(page, 'Schritt 2: Stunden', '26');
        await type(page, 'Schritt 2: Minuten', '10');
        await type(page, 'Schritt 2: Anleitung', 'Abgedeckt im Kühlschrank ruhen lassen.');
        await click(page, 'Schritt 2 nach oben');
        await click(page, 'Schritt 3 entfernen');
        await click(page, /Schritt hinzufügen/);
        await type(page, 'Schritt 3: Titel *', 'Ausbacken');
        await type(page, 'Schritt 3: Minuten', '50');
        await click(page, 'Änderungen speichern', true);
        await expect(page.getByText('„Roggenbrot“ wurde in diesem Browser gespeichert.', { exact: true })).toBeVisible();
        const records = await stored(page);
        assert.equal(records.length, 2);
        const updated = records.find(r => r.id === original.id);
        assert.equal(updated.name, 'Roggenbrot');
        assert.equal(updated.description, 'Mehr Wasser, längere Ruhe.');
        assert.deepEqual(updated.ingredients.slice(0, 2).map(i => i.id), ['flour', 'yeast']);
        assert.deepEqual(updated.ingredients.map(i => i.grams), [600.25, 0.5, 400]);
        assert.equal(updated.ingredients[0].name, 'Roggenmehl');
        assert.notEqual(updated.ingredients[2].id, 'tiny');
        assert.deepEqual(updated.steps.slice(0, 2).map(s => [s.id, s.durationMinutes]), [['rest', 1570], ['mix', 15]]);
        assert.equal(updated.steps[0].instruction, 'Abgedeckt im Kühlschrank ruhen lassen.');
        assert.equal(updated.steps[2].title, 'Ausbacken');
        assert.equal(updated.steps[2].durationMinutes, 50);
        assert.notEqual(updated.steps[2].id, 'bake');
        assert.deepEqual(records.find(r => r.id === other.id), other);
        await page.screenshot({ path: path.join(output, 'edited-desktop.png') });
        await page.reload({ waitUntil: 'networkidle' });
        assert.deepEqual((await stored(page)).find(r => r.id === original.id), updated);
        await click(page, /^Roggenbrot/);
        await click(page, 'Rezept bearbeiten');
        assert.equal(await readField(page, 'Schritt 1: Stunden'), '26');
        assert.equal(await readField(page, 'Schritt 1: Minuten'), '10');
        console.log('PASS: edits replace only the original record; item IDs, order and changes survive reload');

        await type(page, 'Zutat 1: Menge in g *', '0');
        await click(page, 'Änderungen speichern', true);
        await expect(page.getByText('Noch nicht gespeichert. Bitte prüfe die markierten Felder.', { exact: true })).toBeVisible();
        assert.deepEqual((await stored(page)).find(r => r.id === original.id), updated);
        await type(page, 'Zutat 1: Menge in g *', '600,25');
        await type(page, 'Rezeptname *', 'Ackerbrot');
        await page.evaluate(() => {
            const actual = CrumbRecipeStorage;
            globalThis.__rejectUpdate = true;
            globalThis.CrumbRecipeStorage = { ...actual, update: payload => globalThis.__rejectUpdate ? Promise.reject(new Error('Simulated quota failure')) : actual.update(payload) };
        });
        await click(page, 'Änderungen speichern', true);
        await expect(page.getByText(/Das Rezept konnte nicht gespeichert werden/)).toBeVisible();
        assert.deepEqual((await stored(page)).find(r => r.id === original.id), updated);
        assert.equal(await readField(page, 'Rezeptname *'), 'Ackerbrot');
        await page.screenshot({ path: path.join(output, 'editing-error.png') });
        await page.evaluate(() => { globalThis.__rejectUpdate = false; });
        await click(page, 'Änderungen speichern', true);
        await expect(page.getByText('„Ackerbrot“ wurde in diesem Browser gespeichert.', { exact: true })).toBeVisible();
        const sameName = await stored(page);
        assert.equal(sameName.length, 2);
        assert.equal(sameName.find(r => r.id === original.id).name, other.name);
        assert.deepEqual(sameName.find(r => r.id === other.id), other);
        console.log('PASS: invalid edits and write failures preserve stored data; retry updates the correct ID even with duplicate names');

        const missingRejected = await page.evaluate(async recipe => {
            try { await CrumbRecipeStorage.update(JSON.stringify({ ...recipe, id: 'does-not-exist' })); return false; }
            catch { return true; }
        }, updated);
        assert.equal(missingRejected, true);
        assert.deepEqual(await stored(page), sameName);
        await click(page, /Rezept hinzufügen/);
        assert.equal(await readField(page, 'Rezeptname *'), '');
        await click(page, 'Abbrechen', true);
        console.log('PASS: updates cannot create missing records; new-recipe form remains empty after editing');

        await page.setViewportSize({ width: 390, height: 844 });
        await page.waitForTimeout(400);
        await click(page, 'Rezept bearbeiten');
        assert.equal(await readField(page, 'Rezeptname *'), 'Ackerbrot');
        await page.screenshot({ path: path.join(output, 'editing-mobile.png') });
        await type(page, 'Rezeptname *', 'Mobiles Brot');
        await click(page, 'Änderungen speichern', true);
        await expect(page.getByText('„Mobiles Brot“ wurde in diesem Browser gespeichert.', { exact: true })).toBeVisible();
        assert.equal((await stored(page)).find(r => r.id === original.id).name, 'Mobiles Brot');
        assert.deepEqual((await stored(page)).find(r => r.id === other.id), other);
        assert.deepEqual(errors, []);
        console.log('PASS: narrow editing layout and save; no runtime errors. Personal browser data was not accessed.');
    } catch (error) {
        if (page && !page.isClosed()) {
            await page.screenshot({ path: path.join(output, 'editing-failure.png') }).catch(() => {});
            fs.writeFileSync(path.join(output, 'editing-failure.txt'), await page.locator('body').ariaSnapshot().catch(() => ''));
        }
        throw error;
    } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
