const { chromium, expect, assert, fs, path, baseURL, output, reveal, click, type, stored, minimalRecipe } = require('./helpers.cjs');

(async () => {
    const browser = await chromium.launch({ headless: true, ...(process.env.BROWSER_CHANNEL ? { channel: process.env.BROWSER_CHANNEL } : {}) });
    let page;
    try {
        const context = await browser.newContext({ viewport: { width: 1280, height: 1000 } });
        page = await context.newPage();
        const runtimeErrors = [];
        page.on('pageerror', error => runtimeErrors.push(error.message));
        await page.goto(baseURL, { waitUntil: 'networkidle' });
        await click(page, /Rezept hinzufügen/);
        await click(page, 'Speichern', true);
        await expect(page.getByText('Noch nicht gespeichert. Bitte prüfe die markierten Felder.', { exact: true })).toBeVisible();
        assert.equal((await stored(page)).length, 0);
        await type(page, 'Rezeptname *', 'Sauerteig & Körner');
        await type(page, 'Kurzbeschreibung', 'Mit Öl und langer Teigruhe.\nFür zwei Brote.');
        await type(page, 'Zutat 1: Name *', 'Weizenmehl');
        await type(page, 'Zutat 1: Menge in g *', '500');
        await click(page, /Zutat hinzufügen/);
        await type(page, 'Zutat 2: Name *', 'Wasser');
        await type(page, 'Zutat 2: Menge in g *', '350,5');
        await click(page, /Zutat hinzufügen/);
        await type(page, 'Zutat 3: Name *', 'Hefe');
        await type(page, 'Zutat 3: Menge in g *', '0,5');
        await click(page, /Zutat hinzufügen/);
        await click(page, 'Zutat 4 entfernen');
        await type(page, 'Schritt 1: Titel *', 'Ruhen');
        await type(page, 'Schritt 1: Anleitung', 'Abgedeckt kalt stellen.');
        await type(page, 'Schritt 1: Stunden', '24');
        await type(page, 'Schritt 1: Minuten', '30');
        await click(page, /Schritt hinzufügen/);
        await type(page, 'Schritt 2: Titel *', 'Mischen');
        await type(page, 'Schritt 2: Anleitung', 'Alle Zutaten verrühren.');
        await type(page, 'Schritt 2: Minuten', '15');
        await click(page, 'Schritt 2 nach oben');
        await click(page, /Schritt hinzufügen/);
        await type(page, 'Schritt 3: Titel *', 'Entfernen');
        await click(page, 'Schritt 3 entfernen');
        await click(page, /Schritt hinzufügen/);
        await type(page, 'Schritt 3: Titel *', 'Backen');
        await type(page, 'Schritt 3: Minuten', '45');
        await page.screenshot({ path: path.join(output, 'steps-desktop.png') });
        await click(page, 'Speichern', true);
        await expect(page.getByText('„Sauerteig & Körner“ wurde in diesem Browser gespeichert.', { exact: true })).toBeVisible();
        let records = await stored(page);
        assert.equal(records.length, 1);
        assert.deepEqual(records[0].ingredients.map(x => x.grams), [500, 350.5, 0.5]);
        assert.deepEqual(records[0].steps.map(x => [x.title, x.durationMinutes]), [['Mischen', 15], ['Ruhen', 1470], ['Backen', 45]]);
        assert.equal(records[0].steps[0].instruction, 'Alle Zutaten verrühren.');
        assert.equal(records[0].description, 'Mit Öl und langer Teigruhe.\nFür zwei Brote.');
        const firstId = records[0].id;
        console.log('PASS: validation, decimal amounts, add/remove, reorder, atomic save');
        await page.reload({ waitUntil: 'networkidle' });
        await expect(page.getByRole('button', { name: /Sauerteig & Körner/ })).toBeVisible();
        assert.equal((await stored(page))[0].id, firstId);
        await page.screenshot({ path: path.join(output, 'saved-desktop.png') });
        const duplicateRejected = await page.evaluate(async () => {
            const recipe = JSON.parse(await CrumbRecipeStorage.load())[0];
            try { await CrumbRecipeStorage.add(JSON.stringify({ ...recipe, name: 'Must never overwrite' })); return false; }
            catch { return true; }
        });
        assert.equal(duplicateRejected, true);
        assert.equal((await stored(page))[0].name, 'Sauerteig & Körner');
        console.log('PASS: reload preserves identity; duplicate-ID transactions cannot overwrite recipes');

        await click(page, /Rezept hinzufügen/);
        await type(page, 'Rezeptname *', 'Diesen Entwurf verwerfen');
        await click(page, 'Abbrechen', true);
        await page.reload({ waitUntil: 'networkidle' });
        assert.equal((await stored(page)).length, 1);
        console.log('PASS: cancel never writes a draft');

        await click(page, /Rezept hinzufügen/);
        await minimalRecipe(page, 'Sauerteig & Körner');
        await page.evaluate(() => {
            const realStorage = globalThis.CrumbRecipeStorage;
            globalThis.__failWrite = true;
            globalThis.CrumbRecipeStorage = {
                ...realStorage,
                add: payload => globalThis.__failWrite ? Promise.reject(new Error('Simulated quota failure')) : realStorage.add(payload),
            };
        });
        await click(page, 'Speichern', true);
        await expect(page.getByText(/Das Rezept konnte nicht gespeichert werden/)).toBeVisible();
        assert.equal((await stored(page)).length, 1);
        await page.screenshot({ path: path.join(output, 'save-error.png') });
        await page.evaluate(() => {
            globalThis.__failWrite = false;
            const storage = globalThis.CrumbRecipeStorage;
            globalThis.__writeCalls = 0;
            globalThis.CrumbRecipeStorage = { ...storage, add: payload => {
                globalThis.__writeCalls++;
                return new Promise(resolve => { globalThis.__releaseWrite = resolve; }).then(() => storage.add(payload));
            }};
        });
        await click(page, 'Speichern', true);
        await click(page, 'Speichert …', true);
        await click(page, 'Abbrechen', true);
        assert.equal(await page.evaluate(() => globalThis.__writeCalls), 1);
        await expect(page.getByRole('heading', { name: 'Neues Rezept', exact: true })).toBeAttached();
        assert.equal((await stored(page)).length, 1);
        await page.evaluate(() => globalThis.__releaseWrite());
        await expect(page.getByText('„Sauerteig & Körner“ wurde in diesem Browser gespeichert.', { exact: true })).toBeVisible();
        records = await stored(page);
        assert.equal(records.length, 2);
        assert.notEqual(records[0].id, records[1].id);
        assert.equal(records[0].name, records[1].name);
        console.log('PASS: failure preserves draft; retry and duplicate names preserve distinct records');

        // A fresh page routes a failing storage script without altering the real database.
        const errorPage = await context.newPage();
        const storageSource = fs.readFileSync(path.resolve(__dirname, '../../webApp/src/wasmJsMain/resources/recipe-storage.js'), 'utf8');
        await errorPage.route('**/recipe-storage.js', route => route.fulfill({ contentType: 'text/javascript', body: storageSource + '\nconst real = CrumbRecipeStorage; globalThis.CrumbRecipeStorage = {...real, load: () => Promise.reject(new Error("test read error"))};' }));
        await errorPage.goto(baseURL, { waitUntil: 'networkidle' });
        await expect(errorPage.getByText(/Deine Rezepte konnten nicht geladen werden/)).toBeVisible();
        assert.equal((await stored(errorPage)).length, 2);
        await errorPage.evaluate(() => { globalThis.CrumbRecipeStorage = real; });
        await click(errorPage, 'Erneut laden');
        await expect(errorPage.getByText('2 Rezepte', { exact: true })).toBeVisible();
        await errorPage.close();
        console.log('PASS: read failure does not erase recipes; retry restores the list');

        const mobileContext = await browser.newContext({ viewport: { width: 390, height: 844 }, hasTouch: true });
        const mobile = await mobileContext.newPage();
        await mobile.goto(baseURL, { waitUntil: 'networkidle' });
        await mobile.getByRole('button', { name: /Rezept hinzufügen/ }).tap({ force: true });
        await mobile.screenshot({ path: path.join(output, 'editor-mobile.png') });
        await minimalRecipe(mobile, 'Tablet-Brot');
        await mobile.screenshot({ path: path.join(output, 'steps-mobile.png') });
        await click(mobile, 'Speichern', true);
        await expect(mobile.getByText('„Tablet-Brot“ wurde in diesem Browser gespeichert.', { exact: true })).toBeVisible();
        await mobile.reload({ waitUntil: 'networkidle' });
        assert.equal((await stored(mobile))[0].name, 'Tablet-Brot');
        await mobile.close();
        console.log('PASS: narrow touch-capable layout, entry, save and reload');
        assert.deepEqual(runtimeErrors, []);
        console.log('All browser checks passed. Test data stayed in isolated browser contexts.');
    } catch (error) {
        if (page && !page.isClosed()) {
            await page.screenshot({ path: path.join(output, 'failure.png') }).catch(() => {});
            fs.writeFileSync(path.join(output, 'failure.txt'), await page.locator('body').ariaSnapshot().catch(() => ''));
        }
        throw error;
    } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
