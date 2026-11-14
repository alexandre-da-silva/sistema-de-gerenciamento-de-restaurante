const {chromium}=require('../frontend/node_modules/playwright');
const assert=require('node:assert/strict');
(async()=>{
 const browser=await chromium.launch({headless:true});
 const page=await browser.newPage({viewport:{width:1440,height:1000}});const errors=[];page.on('pageerror',e=>errors.push(e.message));
 async function login(email,password){await page.locator('#login [name=email]').fill(email);await page.locator('#login [name=senha]').fill(password);await page.locator('#login button').click();}
 await page.goto('http://localhost:8080');
 await login('client@example.com','integration-client-password');
 await page.locator('.c-home').waitFor();
 assert.equal(await page.locator('.c-categories').count(),0);
 await page.locator('.c-sidebar nav').getByRole('button',{name:'Cardápio',exact:true}).click();
 await page.locator('.c-categories').waitFor();await page.reload();await page.locator('.c-categories').waitFor();
 assert.equal(await page.locator('.c-sidebar nav button.active').innerText(),'Cardápio');assert.equal(await page.locator('#login').count(),0);
 await page.setViewportSize({width:390,height:844});
 assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'Client mobile overflow');
 await page.setViewportSize({width:1440,height:1000});
 await page.locator('.c-sidebar nav').getByRole('button',{name:'Meus Pedidos',exact:true}).click();await page.reload();await page.locator('.c-order-tabs').waitFor();
 assert.equal(await page.locator('.c-sidebar nav button.active').innerText(),'Meus Pedidos');
 await page.locator('.c-logout').click();await page.locator('#login').waitFor();await page.reload();await page.locator('#login').waitFor();
 await login('admin@example.com','integration-admin-password');
 await page.locator('.admin-app').waitFor();await page.locator('[data-nav="Cardápio"]').click();await page.locator('[data-action="create"]').click();await page.locator('#modal.product-editor').waitFor();
 assert.equal(await page.locator('.pe-section').count(),6);await page.locator('#close').click();
 await page.locator('[data-nav="Cozinha"]').click();await page.locator('#kitchen-sector').waitFor();await page.reload();await page.locator('#kitchen-sector').waitFor();
 assert.equal(await page.locator('nav button.active').innerText(),'Cozinha');
 assert.deepEqual(errors,[]);await browser.close();console.log('PASS: real browser client/admin reload, logout, distinct Home, mobile width and product form.');
})().catch(e=>{console.error(e);process.exit(1)});
