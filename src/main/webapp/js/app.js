function readCookie(name) {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}

async function postJson(url, body) {
  const xsrf = readCookie('XSRF-TOKEN');
  const headers = { 'Content-Type': 'application/json' };
  if (xsrf) headers['X-XSRF-TOKEN'] = xsrf;
  const resp = await fetch(url, {
    method: 'POST',
    headers,
    credentials: 'same-origin',
    body: JSON.stringify(body)
  });
  return resp.json().catch(() => ({ status: 'INVALID_JSON', code: resp.status }));
}

async function getJson(url) {
  const resp = await fetch(url, { credentials: 'same-origin' });
  return resp.json().catch(() => ({ status: 'INVALID_JSON', code: resp.status }));
}

async function deleteJson(url, body) {
  const xsrf = readCookie('XSRF-TOKEN');
  const headers = { 'Content-Type': 'application/json' };
  if (xsrf) headers['X-XSRF-TOKEN'] = xsrf;
  const resp = await fetch(url, {
    method: 'DELETE',
    headers,
    credentials: 'same-origin',
    body: JSON.stringify(body)
  });
  return resp.json().catch(() => ({ status: 'INVALID_JSON', code: resp.status }));
}

document.getElementById('btn_register').addEventListener('click', async () => {
  const user = document.getElementById('reg_user').value;
  const pass = document.getElementById('reg_pass').value;
  const first = document.getElementById('reg_first').value;
  const last = document.getElementById('reg_last').value;
  const out = document.getElementById('reg_out');
  const r = await postJson('/register', { user_id: user, password: pass, first_name: first, last_name: last });
  out.textContent = JSON.stringify(r, null, 2);
});

document.getElementById('btn_login').addEventListener('click', async () => {
  const user = document.getElementById('login_user').value;
  const pass = document.getElementById('login_pass').value;
  const out = document.getElementById('login_out');
  const r = await postJson('/login', { user_id: user, password: pass });
  out.textContent = JSON.stringify(r, null, 2);
});

document.getElementById('btn_logout').addEventListener('click', async () => {
  const out = document.getElementById('login_out');
  const r = await postJson('/logout', {});
  out.textContent = JSON.stringify(r, null, 2);
});

document.getElementById('btn_history').addEventListener('click', async () => {
  const out = document.getElementById('history_out');
  const r = await getJson('/history');
  out.textContent = JSON.stringify(r, null, 2);
});
