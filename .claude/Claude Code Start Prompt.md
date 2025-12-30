# Claude Code Start Prompt

> **Read and follow these rules for the entire session.**

---

## ⚠️ CRITICAL: Work ONLY in the Current Directory

**You are operating in ONE repository at a time.**

- **ONLY create/modify files within your current working directory**
- **NEVER access, reference, or modify files in other repositories**
- **If a prompt mentions a specific path, USE THAT PATH — do not substitute**
- **When in doubt, ASK — do not assume**

```bash
# Check where you are BEFORE doing anything
pwd
```

---

## Rule 1: ARCHITECT Role = Super Admin

Every controller must allow ARCHITECT access. Non-negotiable.

```java
// ✅ CORRECT - Always include ARCHITECT
@PreAuthorize("hasRole('ARCHITECT') or hasAuthority('resource:read')")

// ✅ CORRECT - Custom annotation (aspect handles ARCHITECT)
@RequiresPermission("feature:read")

// ❌ WRONG - ARCHITECT gets 403
@PreAuthorize("hasAuthority('resource:read')")
```

---

## Rule 2: Commit After EVERY Fix

Do not batch commits. Each fix = one commit. Immediately.

```bash
# After EVERY bug fix or completed feature:
git add -A && git commit -m "fix: description of what was fixed"
```

---

## Rule 3: No Flyway in Development

```yaml
spring:
  flyway:
    enabled: false
  jpa:
    hibernate:
      ddl-auto: update  # NOT create-drop
```

- ❌ DO NOT create Flyway migration files
- ✅ DO let Hibernate manage schema from entities

---

## Rule 4: 30-Second Timeout Rule

Never wait silently for more than 30 seconds.

- If a command hangs → check logs, report error
- If health check returns 200 → stop waiting, move on
- If build fails → read the error, fix it

---

## Rule 5: No Magic Numbers

Use centralized constants, not inline numbers.

```java
// ✅ CORRECT
factory.setReadTimeout(AppConstants.API_TIMEOUT_MS);

// ❌ WRONG
factory.setReadTimeout(120000);
```

---

## Rule 6: Complete Implementations Only

- **No stubs** — Every function fully implemented
- **No placeholders** — No `// TODO: implement later`
- **No partial features** — If building CRUD, ALL of it works
- **No missing wiring** — Routes, exports, navigation all connected

---

## Rule 7: When Prompt Contains Code, USE IT VERBATIM

If a prompt contains complete code blocks:
- **DO NOT reimplement** — Copy the code exactly
- **DO NOT improvise** — Use what's provided
- **DO NOT "improve"** — The code is intentional

If prompt says "replace with this code" → DELETE old file, CREATE new file with EXACT code from prompt.
```tsx
// If prompt provides this:
export function MyComponent() {
  return Exact code;
}

// ✅ CORRECT: Copy it exactly
// ❌ WRONG: Write your own version that "does the same thing"

---

## Integration Checklist

Every feature must complete ALL items:

- [ ] Route registered in router/App.tsx
- [ ] Component exported from index.ts
- [ ] Navigation entry added (sidebar or menu)
- [ ] API service file exists with typed endpoints
- [ ] Backend endpoint exists and returns data
- [ ] `@PreAuthorize` includes `hasRole('ARCHITECT')`
- [ ] Error states handled
- [ ] Loading states handled
- [ ] Empty states handled

---

## Code Patterns

### Backend Controller

```java
@RestController
@RequestMapping("/api/feature")
@RequiredArgsConstructor
public class FeatureController {
    
    @GetMapping
    @PreAuthorize("hasRole('ARCHITECT') or hasAuthority('feature:read')")
    public ResponseEntity<List<Feature>> list() { }
    
    @PostMapping
    @PreAuthorize("hasRole('ARCHITECT') or hasAuthority('feature:write')")
    public ResponseEntity<Feature> create(@Valid @RequestBody CreateRequest req) { }
}
```

### Frontend Page

```tsx
export default function FeaturePage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['feature'],
    queryFn: featureApi.getAll,
  });
  
  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorState error={error} />;
  if (!data?.length) return <EmptyState />;
  
  return <div>{/* content */}</div>;
}
```

---

## Project Documentation

For every project, maintain:

- `.gitignore` — Create if missing
- `README.md` — Create or update
- `claude.md` — Project-specific notes for AI
- `openapi.yaml` — For Java services

---

## After Completing Any Task

1. **Verify it compiles:** `npm run build` or `mvn compile`
2. **Verify it works:** Test in browser or curl
3. **Commit:** `git add -A && git commit -m "feat/fix: description"`
4. **Report:** List files created/modified and verification results

---

## Common Mistakes to Avoid

| Mistake | Fix |
|---------|-----|
| Working in wrong repository | Run `pwd`, verify against prompt |
| Controller returns 403 for admin | Add `hasRole('ARCHITECT')` |
| Page exists but can't navigate | Add route AND navigation entry |
| Frontend 404 on API call | Verify endpoint URL matches |
| "Module not found" error | Export from index.ts barrel file |
| Fix disappears after changes | Commit immediately after fixing |

---

## What NOT to Do

- ❌ Access repositories outside your current working directory
- ❌ Assume file locations — read them from the prompt
- ❌ Create migration files
- ❌ Leave functions unimplemented
- ❌ Skip commits
- ❌ Wait indefinitely for commands

---

> **Remember:** You are in ONE repo. Stay in your lane. Complete implementations only. No stubs. No TODOs. Ship working code.