import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8091',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'microservice',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'user-login',
});

let initPromise;

export function initKeycloak() {
  if (!initPromise) {
    initPromise = keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    });
  }

  return initPromise;
}

export default keycloak;
