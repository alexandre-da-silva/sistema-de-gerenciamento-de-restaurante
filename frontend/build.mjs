import {build} from 'esbuild';
await build({entryPoints:['src/App.tsx'],bundle:true,minify:true,outfile:'../src/main/resources/static/client/client.js',format:'iife',target:['es2022'],tsconfig:'tsconfig.json',legalComments:'eof',define:{'process.env.NODE_ENV':'"production"'}});
