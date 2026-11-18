/*
 * Fallback para o bundle entregue com esta revisão, quando npm ci não é possível.
 * Reutiliza o runtime React já EMPACOTADO neste arquivo e recompila somente App.tsx
 * e types.ts com o compilador TypeScript disponível no ambiente.
 * Importante: os aliases privados do bundle original não são uma API pública.
 * Para releases, prefira: cd frontend && npm ci && npm run build.
 */
const fs = require('node:fs');
const path = require('node:path');
const ts = require('typescript');
const root=path.resolve(__dirname,'..');
const buildPath=path.join(root,'src/main/resources/static/client/client.js');
const source=fs.readFileSync(buildPath,'utf8');
const start='/* ASTRACHEFE_OFFLINE_PATCH_START */';
const end='/* ASTRACHEFE_OFFLINE_PATCH_END */';
const begin=source.indexOf(start), finish=source.indexOf(end);
if(begin<0||finish<begin)throw Error('Bundle base sem patch offline compatível. Use npm run build.');
const previous=source.slice(begin,finish);
function match(re){const m=previous.match(re);if(!m)throw Error('Aliases do runtime mudaram; use npm run build.');return m[0];}
const icons=match(/const __astrachefeIcons=.*?;\n/s);
const aliases=match(/const __astrachefeRequire=\(name\)=>\{switch\(name\)\{[\s\S]*?\}\};\n/);
function compile(relative){
 const code=fs.readFileSync(path.join(root,relative),'utf8');
 const result=ts.transpileModule(code,{fileName:relative,reportDiagnostics:true,compilerOptions:{
    target:ts.ScriptTarget.ES2022,module:ts.ModuleKind.CommonJS,
    jsx:ts.JsxEmit.ReactJSX,esModuleInterop:true,removeComments:false
 }});
 const errors=(result.diagnostics||[]).filter(d=>d.category===ts.DiagnosticCategory.Error);
 if(errors.length){for(const e of errors)process.stderr.write(ts.flattenDiagnosticMessageText(e.messageText,'\n')+'\n');throw Error('Erro de sintaxe TS/TSX');}
 return result.outputText;
}
const types=compile('frontend/src/types.ts');
const app=compile('frontend/src/App.tsx');
const fresh=[start, '(()=>{',icons,'const __astrachefeTypes={exports:{}};',
 '(function(exports){',types,'})(__astrachefeTypes.exports);',aliases,
 'const __astrachefeModule={exports:{}};',
 '(function(require,module,exports){',app,
 '})(__astrachefeRequire,__astrachefeModule,__astrachefeModule.exports);',
 '})();',''].join('\n');
fs.writeFileSync(buildPath,source.slice(0,begin)+fresh+source.slice(finish));
console.log('Bundle offline atualizado com os novos componentes cliente.');
