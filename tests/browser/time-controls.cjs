const { chromium, expect, assert, path, baseURL, output, reveal, click, type, stored } = require('./helpers.cjs');
(async () => {
    const browser = await chromium.launch({ headless: true, ...(process.env.BROWSER_CHANNEL ? { channel: process.env.BROWSER_CHANNEL } : {}) });
    try {
        const context = await browser.newContext({ viewport: { width: 390, height: 844 }, timezoneId: 'Europe/Berlin' });
        const page = await context.newPage();
        const errors = [];
        page.on('pageerror', e => errors.push(e.message));
        await page.goto(baseURL, { waitUntil: 'networkidle' });
        const recipe = { id: 'controls', name: 'Testbrot', description: '', schemaVersion: 1,
            ingredients: [{ id: 'flour', name: 'Mehl', grams: 500 }],
            steps: [{ id: 'mix', title: 'Mischen', instruction: '', durationMinutes: 60 }] };
        await page.evaluate(r => CrumbRecipeStorage.add(JSON.stringify(r)), recipe);
        await page.reload({ waitUntil: 'networkidle' });
        const date = page.getByRole('textbox', { name: 'Datum (TT.MM.JJJJ)', exact: true });
        const time = page.getByRole('textbox', { name: 'Uhrzeit (HH:MM)', exact: true });
        async function fields(d, t) {
            await expect.poll(() => date.innerText()).toBe(d);
            await expect.poll(() => time.innerText()).toBe(t);
        }
        await type(page, 'Datum (TT.MM.JJJJ)', '31.12.2026');
        await type(page, 'Uhrzeit (HH:MM)', '23:45');
        await click(page, 'Planung berechnen');
        await expect(page.getByRole('button', { name: 'Planung entfernen', exact: true })).toBeAttached();
        await click(page, '+1 Stunde');
        await fields('01.01.2027', '00:45');
        await expect(page.getByRole('button', { name: 'Planung entfernen', exact: true })).toHaveCount(0);
        await click(page, '−1 Stunde');
        await fields('31.12.2026', '23:45');
        await click(page, '+1 Tag');
        await fields('01.01.2027', '23:45');
        await click(page, '−1 Tag');
        await fields('31.12.2026', '23:45');
        await type(page, 'Datum (TT.MM.JJJJ)', '01.03.2028');
        await click(page, '−1 Tag');
        await fields('29.02.2028', '23:45');
        console.log('PASS: narrow day/hour controls preserve minutes/time, cross midnight/year/leap day, and invalidate the old plan');
        await type(page, 'Datum (TT.MM.JJJJ)', 'ungültig');
        await click(page, '+1 Tag');
        await click(page, '−1 Stunde');
        await fields('ungültig', '23:45');
        await type(page, 'Uhrzeit (HH:MM)', '');
        async function checkNow() {
            await click(page, 'Jetzt');
            const local = await page.evaluate(() => {
                const parts = new Intl.DateTimeFormat('de-DE', { timeZone: 'Europe/Berlin', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hourCycle: 'h23' }).formatToParts(new Date());
                const part = name => parts.find(p => p.type === name).value;
                return { date: `${part('day')}.${part('month')}.${part('year')}`, time: `${part('hour')}:${part('minute')}` };
            });
            await fields(local.date, local.time);
            return local;
        }
        await checkNow();
        await type(page, 'Datum (TT.MM.JJJJ)', '01.01.2020');
        await type(page, 'Uhrzeit (HH:MM)', '00:00');
        await checkNow();
        await click(page, 'Zielzeit');
        const current = await checkNow();
        await click(page, 'Planung berechnen');
        await expect.poll(() => page.locator('body').ariaSnapshot()).toContain(`Fertig: ${current.date} · ${current.time}`);
        await click(page, 'Jetzt');
        await expect(page.getByRole('button', { name: 'Planung entfernen', exact: true })).toHaveCount(0);
        await reveal(page, time);
        await page.screenshot({ path: path.join(output, 'time-controls-mobile.png') });
        assert.deepEqual(await stored(page), [recipe]);
        assert.deepEqual(errors, []);
        console.log('PASS: Jetzt repairs invalid fields and reads fresh local time on every click in both modes; records unchanged');
    } finally { await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
