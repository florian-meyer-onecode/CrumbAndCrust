const { chromium, expect, assert, path, baseURL, output, reveal, click, type, stored } = require('./helpers.cjs');
(async () => {
    const browser = await chromium.launch({ headless: true, ...(process.env.BROWSER_CHANNEL ? { channel: process.env.BROWSER_CHANNEL } : {}) });
    try {
        const context = await browser.newContext({ viewport: { width: 1280, height: 1000 }, timezoneId: 'Europe/Berlin' });
        const page = await context.newPage();
        const errors = [];
        page.on('pageerror', e => errors.push(e.message));
        await page.goto(baseURL, { waitUntil: 'networkidle' });
        const recipe = { id: 'plan', name: 'Zeitplanbrot', description: 'Mit Teigruhe und Abkühlen.', schemaVersion: 1,
            ingredients: [{ id: 'flour', name: 'Mehl', grams: 500 }],
            steps: [{ id: 'mix', title: 'Mischen', instruction: 'Alles vermengen.', durationMinutes: 15 },
                { id: 'rest', title: 'Ruhen', instruction: 'Kalt stellen.', durationMinutes: 1500 },
                { id: 'bake', title: 'Backen', instruction: '', durationMinutes: 45 },
                { id: 'cool', title: 'Abkühlen', instruction: 'Auf einem Gitter.', durationMinutes: 60 },
                { id: 'done', title: 'Bereit', instruction: '', durationMinutes: 0 }] };
        const other = { ...recipe, id: 'other', name: 'Anderes Brot' };
        await page.evaluate(async records => { for (const r of records) await CrumbRecipeStorage.add(JSON.stringify(r)); }, [recipe, other]);
        await page.reload({ waitUntil: 'networkidle' });
        await click(page, /^Zeitplanbrot/);
        const text = () => page.locator('body').ariaSnapshot();
        await expect.poll(text).toMatch(/Zeitzone: Europe\/Berlin/);
        await expect(page.getByRole('button', { name: 'Planung entfernen', exact: true })).toHaveCount(0);
        await type(page, 'Datum (TT.MM.JJJJ)', '31.12.2026');
        await type(page, 'Uhrzeit (HH:MM)', '23:30');
        await click(page, 'Planung berechnen');
        await expect.poll(text).toMatch(/Start: 31.12.2026 · 23:30/);
        await expect.poll(text).toMatch(/Fertig: 02.01.2027 · 02:30/);
        await expect.poll(text).toMatch(/Beginn: 02.01.2027 · 01:30/);
        await expect.poll(text).toMatch(/Dauer: 0 Min/);
        await page.screenshot({ path: path.join(output, 'schedule-desktop.png') });
        console.log('PASS: forward plan across year boundary, per-step start/end, cooling and zero-duration endpoint');
        await click(page, 'Zielzeit');
        await expect(page.getByRole('button', { name: 'Planung entfernen', exact: true })).toHaveCount(0);
        await type(page, 'Datum (TT.MM.JJJJ)', '02.01.2027');
        await type(page, 'Uhrzeit (HH:MM)', '02:30');
        await click(page, 'Planung berechnen');
        await expect.poll(text).toMatch(/Start: 31.12.2026 · 23:30/);
        await expect.poll(text).toMatch(/Fertig: 02.01.2027 · 02:30/);
        await click(page, '2× Ansatz');
        await expect.poll(text).toMatch(/Fertig: 02.01.2027 · 02:30/);
        assert.deepEqual(await stored(page), [other, recipe]);
        await click(page, 'Planung entfernen');
        await expect.poll(text).not.toMatch(/Beginn: /);
        console.log('PASS: backward calculation matches forward plan; scaling keeps times; remove returns to durations');
        await type(page, 'Datum (TT.MM.JJJJ)', '29.02.2026');
        await type(page, 'Uhrzeit (HH:MM)', '24:00');
        await click(page, 'Planung berechnen');
        await expect.poll(text).toMatch(/Gib ein gültiges Datum/);
        await expect.poll(text).toMatch(/Gib eine Uhrzeit/);
        await type(page, 'Datum (TT.MM.JJJJ)', '29.03.2026');
        await type(page, 'Uhrzeit (HH:MM)', '02:30');
        await click(page, 'Planung berechnen');
        await expect.poll(text).toMatch(/wegen der Zeitumstellung nicht/);
        await click(page, 'Startzeit');
        await type(page, 'Uhrzeit (HH:MM)', '01:30');
        await click(page, 'Planung berechnen');
        await expect.poll(text).toMatch(/Fertig: 30.03.2026 · 05:30/);
        await expect.poll(text).toMatch(/UTC\+01:00/);
        await expect.poll(text).toMatch(/UTC\+02:00/);
        console.log('PASS: invalid date/time and spring gap rejected; real elapsed durations survive DST in browser');
        await click(page, /^Anderes Brot/);
        await expect(page.getByRole('button', { name: 'Planung entfernen', exact: true })).toHaveCount(0);
        await click(page, /^Zeitplanbrot/);
        await type(page, 'Datum (TT.MM.JJJJ)', '25.10.2026');
        await type(page, 'Uhrzeit (HH:MM)', '02:30');
        await click(page, 'Planung berechnen');
        await expect.poll(text).toMatch(/Start: 25.10.2026 · 02:30 \(UTC\+02:00\)/);
        await expect.poll(text).toMatch(/Fertig: 26.10.2026 · 04:30 \(UTC\+01:00\)/);
        await page.reload({ waitUntil: 'networkidle' });
        await click(page, /^Zeitplanbrot/);
        await expect(page.getByRole('button', { name: 'Planung entfernen', exact: true })).toHaveCount(0);
        await page.setViewportSize({ width: 390, height: 844 });
        await click(page, 'Zielzeit');
        await type(page, 'Datum (TT.MM.JJJJ)', '10.09.2026');
        await type(page, 'Uhrzeit (HH:MM)', '12:00');
        await click(page, 'Planung berechnen');
        await expect.poll(text).toMatch(/Start: 09.09.2026 · 09:00/);
        await expect.poll(text).toMatch(/Fertig: 10.09.2026 · 12:00/);
        await reveal(page, page.getByRole('button', { name: 'Planung entfernen', exact: true }));
        await page.screenshot({ path: path.join(output, 'schedule-mobile-controls.png') });
        await reveal(page, page.getByRole('heading', { name: 'Ruhen', exact: true }));
        await page.screenshot({ path: path.join(output, 'schedule-mobile-timeline.png') });
        assert.deepEqual(await stored(page), [other, recipe]);
        assert.deepEqual(errors, []);
        console.log('PASS: autumn overlap uses first occurrence; selection/reload clear planning; narrow target planning works without changing records');
    } finally { await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
