/* Browser persistence boundary. All successful writes resolve after transaction commit. */
(() => {
    "use strict";
    const databaseName = "crumb-and-crust";
    const storeName = "recipes";

    function openDatabase() {
        return new Promise((resolve, reject) => {
            if (!globalThis.indexedDB) {
                reject(new Error("Browser storage is unavailable."));
                return;
            }
            let rejected = false;
            const request = indexedDB.open(databaseName, 1);
            request.onupgradeneeded = () => {
                if (!request.result.objectStoreNames.contains(storeName)) {
                    request.result.createObjectStore(storeName, { keyPath: "id" });
                }
            };
            request.onerror = () => reject(request.error);
            request.onblocked = () => {
                rejected = true;
                reject(new Error("Close other app tabs and retry."));
            };
            request.onsuccess = () => {
                const database = request.result;
                if (rejected) { database.close(); return; }
                database.onversionchange = () => database.close();
                resolve(database);
            };
        });
    }

    async function transact(mode, operation) {
        const database = await openDatabase();
        try {
            return await new Promise((resolve, reject) => {
                const transaction = database.transaction(storeName, mode);
                let result;
                transaction.oncomplete = () => resolve(result);
                transaction.onabort = () => reject(transaction.error || new Error("Storage transaction aborted."));
                transaction.onerror = () => { /* IndexedDB aborts failed requests by default. */ };
                try {
                    const request = operation(transaction.objectStore(storeName), transaction);
                    request.onsuccess = () => { result = request.result; };
                } catch (error) {
                    transaction.abort();
                    reject(error);
                }
            });
        } finally {
            database.close();
        }
    }

    globalThis.CrumbRecipeStorage = Object.freeze({
        async load() {
            return JSON.stringify(await transact("readonly", store => store.getAll()));
        },
        async update(payload) {
            const recipe = JSON.parse(payload);
            if (!recipe || typeof recipe.id !== "string" || !recipe.id) throw new Error("Missing recipe identity.");
            // Check existence and replace within one transaction. An update must not create a new recipe.
            await transact("readwrite", (store, transaction) => {
                const request = store.get(recipe.id);
                request.addEventListener("success", () => {
                    if (request.result === undefined) { transaction.abort(); return; }
                    store.put(recipe);
                });
                return request;
            });
            return "saved";
        },
        async add(payload) {
            const recipe = JSON.parse(payload);
            if (!recipe || typeof recipe.id !== "string" || !recipe.id) throw new Error("Missing recipe identity.");
            // add() intentionally rejects duplicate IDs; creating a recipe must never overwrite one.
            await transact("readwrite", store => store.add(recipe));
            return "saved";
        },
    });
})();
