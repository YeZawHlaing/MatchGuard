## MatchGuard Git Branching & Workflow Guide 

To keep our development organized, prevent code conflicts, and map cleanly to our GitHub Issues, we follow a strict One Branch Per Issue workflow.

---

### Branch Naming Convention
Every feature, bugfix, or task must be developed in its own dedicated branch. Please use the following format:

```text
feature/<issue_short_name>_<your_name>
```

**Examples:**

* feature/ai_scam_detection_phyuhninaung


* feature/jwt_auth_kyawzayerhein


* feature/qr_generation_winpapaphyo
---

### Step-by-Step Development Workflow

**Step 1: Switch to** `main (or base branch)` **and pull the latest changes**
Always start from an updated base branch to avoid outdated code conflicts.

```text
git checkout main
git pull origin main
```

---

**Step 2: Create your feature branch for the specific issue**
Create and switch to your new branch using the naming convention.

```text
git checkout -b feature/ai_scam_detection_yezawhlaing
```
---

**Step 3: Write code and make atomic commits**
Make small, meaningful commits referencing the issue or task description.

```text
git add .
git commit -m "feat: integrate Gemini API for product scam detection and trust scoring"
```
---

**Step 4: Push your branch to GitHub**
Push your local branch to the remote repository.

````text
git push origin feature/ai_scam_detection_yezawhlaing
````

---

**Step 5: Open a Pull Request (PR)**


1. Go to the GitHub repository.


2. Click Compare & pull request for your newly pushed branch.


3. Set the base branch to main.


4. Add a clear description linking the PR to its respective GitHub Issue (e.g., Closes #3B).


5. Request a code review from a teammate before merging.
