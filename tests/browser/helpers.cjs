// Runs against the local app using an isolated profile, never the user's browser data.
const { chromium, expect } = require('playwright/test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const baseURL = process.env.APP_URL || 'http://localhost:8080';
const output = path.resolve(__dirname, '../../build/browser-tests');
fs.mkdirSync(output, { recursive: true });

async function reveal(page, locator) {
    await locator.waitFor({ state: 'attached' });
    const height = page.viewportSize().height;
    const visible = async () => {
        const box = await locator.boundingBox();
        return box && box.width > 0 && box.height > 0 && box.x >= 0 && box.x + box.width <= page.viewportSize().width && box.y >= 82 && box.y + box.height <= height - 86;
    };
    if (await visible()) return;
    await page.mouse.move(page.viewportSize().width - 8, height / 2);
    await page.mouse.wheel(0, -30000);
    await page.waitForTimeout(500);
    for (let i = 0; i < 50; i++) {
        if (await visible()) return;
        await page.mouse.wheel(0, Math.max(160, (height - 200) / 2));
        await page.waitForTimeout(250);
    }
    throw new Error('Could not reveal ' + locator);
}
async function click(page, name, sticky = false) {
    const locator = page.getByRole('button', { name, exact: typeof name === 'string' });
    if (!sticky) await reveal(page, locator);
    // Compose's semantic DOM sits behind its rendering canvas; click the actual position.
    const box = await locator.boundingBox();
    await page.mouse.click(box.x + box.width / 2, box.y + box.height / 2);
    await page.waitForTimeout(100);
}
async function type(page, name, value) {
    const locator = page.getByRole('textbox', { name, exact: true });
    await reveal(page, locator);
    const box = await locator.boundingBox();
    await page.mouse.click(box.x + box.width / 2, box.y + box.height / 2);
    await page.waitForTimeout(150);
    await page.keyboard.press('ControlOrMeta+A');
    await page.keyboard.insertText(value);
    await page.waitForTimeout(150);
    const actual = await page.evaluate(() => document.activeElement?.value);
    if (actual !== undefined) assert.equal(actual, value, name);
}
async function stored(page) {
    return page.evaluate(() => new Promise((resolve, reject) => {
        const request = indexedDB.open('crumb-and-crust', 1);
        request.onerror = () => reject(request.error);
        request.onsuccess = () => {
            const db = request.result;
            const transaction = db.transaction('recipes', 'readonly');
            const read = transaction.objectStore('recipes').getAll();
            let records;
            read.onsuccess = () => { records = read.result; };
            transaction.oncomplete = () => { db.close(); resolve(records); };
            transaction.onabort = () => { db.close(); reject(transaction.error); };
        };
    }));
}
async function minimalRecipe(page, name) {
    await type(page, 'Rezeptname *', name);
    await type(page, 'Zutat 1: Name *', 'Mehl');
    await type(page, 'Zutat 1: Menge in g *', '500');
    await type(page, 'Schritt 1: Titel *', 'Mischen');
    await type(page, 'Schritt 1: Minuten', '15');
}

module.exports = { chromium, expect, assert, fs, path, baseURL, output, reveal, click, type, stored, minimalRecipe };
