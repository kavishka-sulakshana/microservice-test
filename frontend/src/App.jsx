import { useEffect, useMemo, useState } from 'react';
import { KeyRound, LogIn, LogOut, RefreshCw, Send, ShieldCheck, UserRound } from 'lucide-react';
import keycloak, { initKeycloak } from './keycloak';

const gatewayUrl = import.meta.env.VITE_GATEWAY_URL ?? 'http://localhost:8080';

function App() {
  const [ready, setReady] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [profile, setProfile] = useState(null);
  const [tokenPreview, setTokenPreview] = useState('');
  const [orderId, setOrderId] = useState('');
  const [apiResult, setApiResult] = useState(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let cancelled = false;

    initKeycloak()
      .then((isAuthenticated) => {
        if (cancelled) return;
        setAuthenticated(isAuthenticated);
        setTokenPreview(formatToken(keycloak.token));
        setReady(true);
        if (isAuthenticated) {
          loadProfile();
        }
      })
      .catch((error) => {
        if (cancelled) return;
        setApiResult({
          ok: false,
          status: 'Keycloak init failed',
          body: error?.message ?? String(error),
        });
        setReady(true);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const roles = useMemo(() => {
    return keycloak.tokenParsed?.realm_access?.roles?.filter((role) => role === 'user' || role === 'service') ?? [];
  }, [authenticated, tokenPreview]);

  async function loadProfile() {
    try {
      const loadedProfile = await keycloak.loadUserProfile();
      setProfile(loadedProfile);
    } catch {
      setProfile(null);
    }
  }

  async function refreshToken() {
    setBusy(true);
    try {
      await keycloak.updateToken(30);
      setTokenPreview(formatToken(keycloak.token));
      setApiResult({
        ok: true,
        status: 'Token refreshed',
        body: keycloak.tokenParsed,
      });
    } catch (error) {
      setApiResult({
        ok: false,
        status: 'Refresh failed',
        body: error?.message ?? String(error),
      });
    } finally {
      setBusy(false);
    }
  }

  async function createOrder() {
    setBusy(true);
    setApiResult(null);

    const response = await callApi('/api/orders', {
      method: 'POST',
      body: JSON.stringify({
        userId: keycloak.subject ?? profile?.username ?? 'demo',
        totalAmount: 25,
        items: [
          {
            productId: 'demo-product',
            quantity: 1,
          },
        ],
      }),
    });

    if (response.ok && response.body?.id) {
      setOrderId(response.body.id);
    }

    setApiResult(response);
    setBusy(false);
  }

  async function getOrder() {
    if (!orderId.trim()) return;
    setBusy(true);
    setApiResult(await callApi(`/api/orders/${orderId.trim()}`));
    setBusy(false);
  }

  async function getPayment() {
    if (!orderId.trim()) return;
    setBusy(true);
    setApiResult(await callApi(`/api/payments/${orderId.trim()}`));
    setBusy(false);
  }

  async function callApi(path, options = {}) {
    try {
      await keycloak.updateToken(30);
      setTokenPreview(formatToken(keycloak.token));

      const response = await fetch(`${gatewayUrl}${path}`, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${keycloak.token}`,
          ...(options.headers ?? {}),
        },
      });

      const text = await response.text();
      const body = text ? JSON.parse(text) : null;
      return {
        ok: response.ok,
        status: `${response.status} ${response.statusText}`,
        body,
      };
    } catch (error) {
      return {
        ok: false,
        status: 'Request failed',
        body: error?.message ?? String(error),
      };
    }
  }

  if (!ready) {
    return (
      <main className="shell">
        <section className="panel loading">Connecting to Keycloak...</section>
      </main>
    );
  }

  return (
    <main className="shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Keycloak auth demo</p>
          <h1>Microservice Example</h1>
        </div>
        <div className="actions">
          {authenticated ? (
            <>
              <button type="button" className="iconButton" onClick={refreshToken} disabled={busy} title="Refresh token">
                <RefreshCw size={18} />
              </button>
              <button type="button" className="secondary" onClick={() => keycloak.logout({ redirectUri: window.location.origin })}>
                <LogOut size={18} />
                Logout
              </button>
            </>
          ) : (
            <button type="button" onClick={() => keycloak.login()}>
              <LogIn size={18} />
              Login
            </button>
          )}
        </div>
      </header>

      <section className="grid">
        <article className="panel identity">
          <div className="panelTitle">
            <UserRound size={18} />
            <h2>Session</h2>
          </div>
          {authenticated ? (
            <div className="stack">
              <div className="status ok">
                <ShieldCheck size={18} />
                Authenticated
              </div>
              <dl>
                <dt>Subject</dt>
                <dd>{keycloak.subject}</dd>
                <dt>User</dt>
                <dd>{profile?.username ?? keycloak.tokenParsed?.preferred_username ?? 'Unknown'}</dd>
                <dt>Email</dt>
                <dd>{profile?.email ?? keycloak.tokenParsed?.email ?? 'Not set'}</dd>
                <dt>Roles</dt>
                <dd>{roles.length ? roles.join(', ') : 'No app roles'}</dd>
              </dl>
            </div>
          ) : (
            <div className="empty">
              <KeyRound size={28} />
              <p>Login with the local `demo` user to call protected APIs.</p>
            </div>
          )}
        </article>

        <article className="panel">
          <div className="panelTitle">
            <Send size={18} />
            <h2>API Calls</h2>
          </div>
          <div className="formRow">
            <label htmlFor="orderId">Order ID</label>
            <input
              id="orderId"
              value={orderId}
              onChange={(event) => setOrderId(event.target.value)}
              placeholder="Create an order or paste an ID"
            />
          </div>
          <div className="buttonGrid">
            <button type="button" onClick={createOrder} disabled={!authenticated || busy}>
              Create Order
            </button>
            <button type="button" className="secondary" onClick={getOrder} disabled={!authenticated || busy || !orderId.trim()}>
              Get Order
            </button>
            <button type="button" className="secondary" onClick={getPayment} disabled={!authenticated || busy || !orderId.trim()}>
              Get Payment
            </button>
          </div>
        </article>

        <article className="panel token">
          <div className="panelTitle">
            <KeyRound size={18} />
            <h2>Access Token</h2>
          </div>
          <pre>{authenticated ? tokenPreview : 'No token. Login first.'}</pre>
        </article>

        <article className="panel result">
          <div className="panelTitle">
            <ShieldCheck size={18} />
            <h2>Response</h2>
          </div>
          <div className={apiResult?.ok ? 'responseStatus success' : 'responseStatus'}>
            {apiResult?.status ?? 'No request yet'}
          </div>
          <pre>{apiResult ? JSON.stringify(apiResult.body, null, 2) : 'Call an API to see the protected response.'}</pre>
        </article>
      </section>
    </main>
  );
}

function formatToken(token) {
  if (!token) return '';
  if (token.length <= 96) return token;
  return `${token.slice(0, 48)}...${token.slice(-48)}`;
}

export default App;
