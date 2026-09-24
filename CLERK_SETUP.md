# Clerk Backend Configuration

The API is configured as a Spring Security OAuth2 Resource Server. Clerk signs the frontend session JWT, and Spring Security validates that JWT before protected ElectroMart endpoints execute.

Required Render environment variable:

```
CLERK_ISSUER_URL=https://your-instance.clerk.accounts.dev
```

The Clerk session token must contain the custom claim:

```json
{
  "email": "{{user.primary_email_address}}"
}
```

The claim is configured in Clerk Dashboard -> Sessions -> Customize session token.

The backend does not need the Clerk Secret Key for normal request authentication. It validates the bearer JWT using Clerk's published signing keys discovered from the issuer.

After authentication, React calls:

```
POST /api/auth/clerk/sync
```

The endpoint creates/updates the local ElectroMart User record and stores the Clerk user id. Existing ElectroMart business data such as carts, wishlists and orders remains tied to that local user.

Do not commit Clerk secret keys or other private credentials.
