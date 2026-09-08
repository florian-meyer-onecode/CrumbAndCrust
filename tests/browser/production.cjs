// Serve the finished WebAssembly distribution so live rebuilds cannot interrupt form tests.
const http = require('node:http');
const fs = require('node:fs/promises');
const path = require('node:path');
const { spawn } = require('node:child_process');
const root = path.resolve(__dirname, '../../webApp/build/dist/wasmJs/productionExecutable');
const server = http.createServer(async (request, response) => {
    try {
        const pathname = decodeURIComponent(new URL(request.url, 'http://localhost').pathname);
        const file = path.resolve(root, '.' + (pathname === '/' ? '/index.html' : pathname));
        if (!file.startsWith(root + path.sep)) { response.writeHead(403).end(); return; }
        const data = await fs.readFile(file);
        const types = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript', '.wasm': 'application/wasm', '.ttf': 'font/ttf' };
        response.writeHead(200, { 'Content-Type': types[path.extname(file)] || 'application/octet-stream' });
        response.end(data);
    } catch { response.writeHead(404).end(); }
});
function run(script, url) {
    return new Promise((resolve, reject) => {
        const child = spawn(process.execPath, [path.join(__dirname, script)], {
            env: { ...process.env, APP_URL: url }, stdio: 'inherit', windowsHide: true,
        });
        child.on('error', reject);
        child.on('exit', code => code === 0 ? resolve() : reject(new Error(`${script} exited with ${code}`)));
    });
}
(async () => {
    await fs.access(path.join(root, 'index.html'));
    await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
    try {
        const url = `http://127.0.0.1:${server.address().port}`;
        await run('recipes.cjs', url);
        await run('editing.cjs', url);
        await run('details.cjs', url);
        await run('scheduling.cjs', url);
        await run('time-controls.cjs', url);
    } finally { server.close(); }
})().catch(error => { console.error(error); server.close(); process.exitCode = 1; });
