# 🍱 EatYaar.in

> **Food is a resource. Not charity. Not waste.**

EatYaar is a community food sharing platform that connects people with surplus food to people who want it — no money, no delivery, no middlemen. A student in a hostel can claim biryani from a wedding two streets away. A home cook can share extra rotis with a neighbor. No stigma. No awkwardness. Just food moving from where it's excess to where it's wanted.

🌐 **Live:** [eatyaar.in](https://eatyaar.vercel.app)

---

## 🧠 Why EatYaar?

**40% of all food produced globally is wasted.**

In India alone, ~67 million tonnes of food goes to waste every year. At every wedding, event, office canteen, and hostel mess — food gets thrown away not because there's no one to eat it, but because there's no easy way to connect the two.

Existing solutions treat this as a charity problem — food from the rich going to the poor. That model creates stigma and limits adoption.

**EatYaar reframes it entirely:**

- Anyone can give. Anyone can take.
- Rich, poor, student, professional — food is just food.
- Every meal saved is water saved, energy saved, CO₂ saved.
- Think of it like ride sharing — but for food.

---

## ✨ Features

**Core**
- 📱 Phone + OTP authentication (no passwords)
- 🍱 Post food listings with pickup window, area, servings, food type
- 🔍 Browse all available food, filter by city
- 🙋 Claim food listings
- ✅ Giver approves / rejects claims
- 🏠 Exact address revealed only after approval (privacy by design)
- 📦 Mark food as picked up
- ⭐ Rate givers after pickup — builds trust scores

**UX**
- 🔔 Real-time claim notifications (polling every 30s)
- 📲 New claim banner popup
- 🍞 Toast notifications (no browser alerts)
- 📊 Impact stats — meals shared, meals saved, total users
- 🌍 Personal impact — CO₂ and water saved per user
- ♾️ Load More feed (no pagination friction)
- ⏰ Urgency timer on food cards

**Trust & Safety**
- Phone verification mandatory
- Exact address hidden from public
- Dual ratings system
- Trust score per user
- Terms of Service and Privacy Policy

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17 + Spring Boot 3 |
| Database | PostgreSQL 15 |
| Auth | JWT + OTP (email delivery via Hostinger SMTP) |
| Frontend | React 18 + Vite |
| Styling | Tailwind CSS |
| Backend Hosting | AWS EC2 (Ubuntu 22.04) |
| Frontend Hosting | Vercel |
| CI/CD | GitHub Actions |
| Container (Dev) | Docker + OrbStack |

---

## 🏗️ Architecture

```
User Browser
     │
     ▼
Vercel CDN (React + Vite)
     │  HTTPS
     ▼
AWS EC2 — Spring Boot (port 8080)
     │
     ▼
PostgreSQL (same EC2)
```

**CI/CD Pipeline:**
```
Git push to main
     │
     ▼
GitHub Actions triggered
     │
     ├── Build Spring Boot JAR (Maven)
     ├── Copy JAR to EC2 via SCP
     └── SSH into EC2 → restart systemd service
```

---

## 📁 Project Structure

### Backend (`eatyaar-backend`)
```
src/main/java/com/eatyaar/
├── config/          # Security, CORS, JWT filter
├── controller/      # REST API endpoints
├── dto/             # Request and Response objects
│   ├── request/
│   └── response/
├── entity/          # JPA database entities
├── exception/       # Global exception handling
├── repository/      # Spring Data JPA interfaces
├── service/         # Business logic
└── util/            # JWT utility, OTP store
```

### Frontend (`eatyaar-frontend`)
```
src/
├── api/             # Axios instance with JWT interceptor
├── components/      # Navbar, FoodCard, ClaimBanner, Footer
├── context/         # Auth, Toast, Notification providers
└── pages/           # All route pages
```

---

## 🗄️ Data Model

```
User
 └── posts many FoodListings
 └── makes many Claims
 └── receives many Ratings

FoodListing
 └── has many Claims
 └── has one Rating (per pickup)

Claim
 └── connects User ↔ FoodListing
 └── status: PENDING → APPROVED → PICKED_UP
                     → REJECTED

Rating
 └── given after PICKED_UP
 └── updates giver's trust score
```

**Listing Status Flow:**
```
AVAILABLE → CLAIMED → COMPLETED
                    ↘ EXPIRED
```

**Claim Status Flow:**
```
PENDING → APPROVED → PICKED_UP
        → REJECTED
```

---

## 🔌 API Reference

### Auth
```
POST   /api/auth/send-otp          Send OTP to phone number
POST   /api/auth/verify-otp        Verify OTP → receive JWT token
PATCH  /api/auth/profile           Complete user profile
```

### Food Listings
```
GET    /api/listings               Get all available listings (optional ?city=)
GET    /api/listings/all           Get all without filter
GET    /api/listings/my            My posted listings (auth)
GET    /api/listings/{id}          Get one listing
POST   /api/listings               Create listing (auth)
PATCH  /api/listings/{id}/expire   Expire a listing (owner)
DELETE /api/listings/{id}          Delete listing (owner)
```

### Claims
```
POST   /api/claims                 Claim a listing (auth)
GET    /api/claims/my              My claims as taker (auth)
GET    /api/claims/received        Claims on my listings as giver (auth)
PATCH  /api/claims/{id}/approve    Approve a claim (giver)
PATCH  /api/claims/{id}/reject     Reject a claim (giver)
PATCH  /api/claims/{id}/picked-up  Mark as picked up (taker)
```

### Ratings & Users
```
POST   /api/ratings                Submit rating after pickup (auth)
GET    /api/users/me               My profile (auth)
GET    /api/users/{id}/profile     Any user's public profile
GET    /api/users/{id}/ratings     Ratings received by a user
```

### Stats
```
GET    /api/stats/global           Global impact stats (public)
```

---

## 🚀 Local Development Setup

### Prerequisites
- Java 17
- Docker + OrbStack (Mac) or Docker Desktop
- Node.js 18+
- IntelliJ IDEA

### 1. Clone the repos

```bash
git clone https://github.com/YOUR_USERNAME/eatyaar-backend.git
git clone https://github.com/YOUR_USERNAME/eatyaar-frontend.git
```

### 2. Start PostgreSQL via Docker

```bash
cd eatyaar-backend
docker compose up -d
```

`docker-compose.yml` starts:
- PostgreSQL on port `5432`
- pgAdmin on port `5050` (http://localhost:5050)

### 3. Configure environment variables

Create `.env` in the backend root:

```env
DB_USERNAME=eatyaar_user
DB_PASSWORD=eatyaar123
JWT_SECRET=your_min_32_char_secret_here
MAIL_PASSWORD=your_smtp_password
MAIL_TEST_RECIPIENT=your@email.com
```

> `.env` is gitignored. Never commit real credentials.

### 4. Run the backend

Open `EatYaarApplication.java` in IntelliJ and click Run.

Backend starts at: `http://localhost:8080`

### 5. Run the frontend

```bash
cd eatyaar-frontend
npm install
```

Create `.env.local`:
```
VITE_API_URL=http://localhost:8080/api
```

```bash
npm run dev
```

Frontend starts at: `http://localhost:5173`

---

## 🧪 Testing the Full Flow

Use Postman or the running frontend:

```
1. POST /api/auth/send-otp        → { "phone": "9876543210" }
2. Check console/email for OTP
3. POST /api/auth/verify-otp      → { "phone": "...", "otp": "..." }
4. PATCH /api/auth/profile        → { "name": "...", "city": "...", "area": "..." }
5. POST /api/listings             → Create food listing (with JWT token)
6. GET  /api/listings             → Browse all listings
7. POST /api/claims               → Claim a listing (different user)
8. PATCH /api/claims/{id}/approve → Approve claim (giver)
9. PATCH /api/claims/{id}/picked-up → Mark picked up (taker)
10. POST /api/ratings             → Submit rating
11. GET /api/users/{id}/profile   → Verify trust score updated
```

---

## ⚙️ CI/CD — GitHub Actions

Every push to `main` automatically:
1. Builds the Spring Boot JAR via Maven
2. Copies JAR to EC2 via SCP
3. SSHes into EC2 and restarts the systemd service

**Required GitHub Secrets:**

| Secret | Description |
|---|---|
| `EC2_HOST` | EC2 public IP |
| `EC2_USERNAME` | `ubuntu` |
| `EC2_PRIVATE_KEY` | SSH private key |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing secret |
| `MAIL_PASSWORD` | SMTP password |
| `MAIL_TEST_RECIPIENT` | OTP delivery email |

---

## 🔐 Security Approach

| Concern | Solution |
|---|---|
| Authentication | OTP-based, no passwords stored |
| Authorization | JWT tokens, validated on every request |
| Address privacy | Exact address hidden until claim approved |
| Secrets | Environment variables, never in code |
| CORS | Restricted to known origins |
| Token | 7-day expiry, signed with HS256 |
| OTP | 5-minute expiry, in-memory only |

**Known improvements planned:**
- OTP brute force protection (3 attempt limit)
- HTTPS via Nginx + Let's Encrypt
- Backend pagination via Spring Pageable
- Rate limiting on auth endpoints
- Unit and integration test coverage

---

## 🌱 Roadmap

**v1.1 — Hardening**
- [ ] OTP attempt limiting
- [ ] HTTPS on EC2
- [ ] Specific exception classes
- [ ] Backend pagination
- [ ] Request size limits
- [ ] Logging with SLF4J

**v1.2 — Testing**
- [ ] Unit tests — AuthService, ClaimService
- [ ] Integration tests with Testcontainers
- [ ] API contract tests

**v1.3 — Features**
- [ ] Real SMS OTP via Fast2SMS
- [ ] Real-time notifications via WebSocket
- [ ] Map view of nearby food
- [ ] Community groups (college, apartment)
- [ ] Mobile app (React Native)

---

## 🤔 Known Limitations

- OTP delivered via email (not SMS) in current version — for testing purposes
- No real-time push notifications — polling every 30 seconds
- `ddl-auto=update` used — Flyway migrations planned
- No unit test coverage yet — in roadmap

---

## 👨‍💻 Author

Built by **[Muneeb Kattody]**
- GitHub: [@muneebkattody](https://github.com/muneebkattody)

---

## 📄 License

**Proprietary — All Rights Reserved**

Copyright (c) 2026 Muneeb Kattody. This project is not open source.

Viewing this code is permitted for portfolio evaluation purposes only.
No permission is granted to use, copy, modify, or deploy this software.

See [LICENSE](LICENSE) for full terms.

---

> *EatYaar started as a portfolio project and grew into something that might actually matter. 40% of food gets wasted while people two streets away are hungry. That gap shouldn't exist.*