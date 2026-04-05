# Git Interview Questions & Answers

## Table of Contents
1. [Basic Git Questions](#basic-git-questions)
2. [Git Commands](#git-commands)
3. [Branching & Merging](#branching--merging)
4. [Git Workflow](#git-workflow)
5. [Advanced Git Concepts](#advanced-git-concepts)
6. [Troubleshooting & Scenarios](#troubleshooting--scenarios)

---

## Basic Git Questions

### 1. What is Git? Why is it used?

**Answer:**
Git is a distributed version control system that tracks changes in source code during software development. It allows multiple developers to work together on the same project.

**Key Benefits:**
- Distributed architecture (every developer has full history)
- Branching and merging capabilities
- Fast performance
- Data integrity through SHA-1 hashing
- Offline work capability
- Free and open source

---

### 2. What is the difference between Git and GitHub?

**Answer:**
- **Git** is a version control system (software tool)
- **GitHub** is a cloud-based hosting service for Git repositories
- Other Git hosting services: GitLab, Bitbucket, Azure DevOps

GitHub adds features like:
- Web-based repository hosting
- Pull requests and code review
- Issue tracking
- CI/CD integration
- Collaboration tools

---

### 3. What is a repository in Git?

**Answer:**
A repository (repo) is a storage location where your project files and their complete version history are stored. It contains:
- All project files
- Complete commit history
- Branches and tags
- Configuration files (.git folder)

**Types:**
- **Local Repository**: On your machine
- **Remote Repository**: On a server (GitHub, GitLab, etc.)

---

### 4. Explain the Git workflow (Working Directory, Staging Area, Repository)

**Answer:**
```
Working Directory → Staging Area → Local Repository → Remote Repository
      ↓                 ↓                ↓                  ↓
  (Untracked)      (git add)       (git commit)       (git push)
```

1. **Working Directory**: Where you modify files
2. **Staging Area (Index)**: Files ready to be committed
3. **Local Repository**: Committed changes with history
4. **Remote Repository**: Shared repository on server

---

### 5. What is the difference between git pull and git fetch?

**Answer:**

| git fetch | git pull |
|-----------|----------|
| Downloads changes from remote | Downloads + merges changes |
| Doesn't modify working directory | Updates working directory |
| Safe to run anytime | Can cause merge conflicts |
| `git fetch origin` | `git pull = git fetch + git merge` |

**When to use:**
- Use `git fetch` to review changes before merging
- Use `git pull` when you trust the remote changes

---

## Git Commands

### 6. What are the most important Git commands you use daily?

**Answer:**
```bash
# Repository setup
git init                    # Initialize new repository
git clone <url>            # Clone remote repository

# Basic workflow
git status                 # Check file status
git add <file>            # Stage files
git add .                 # Stage all changes
git commit -m "message"   # Commit changes
git push origin <branch>  # Push to remote

# Branching
git branch                # List branches
git branch <name>         # Create branch
git checkout <branch>     # Switch branch
git checkout -b <branch>  # Create and switch
git merge <branch>        # Merge branch

# Updates
git pull                  # Fetch and merge
git fetch                 # Download changes

# History
git log                   # View commit history
git log --oneline        # Compact history
git diff                  # Show changes
```

---

### 7. How do you undo changes in Git?

**Answer:**

**Unstage a file (undo git add):**
```bash
git reset HEAD <file>
git restore --staged <file>  # Newer command
```

**Discard changes in working directory:**
```bash
git checkout -- <file>
git restore <file>  # Newer command
```

**Undo last commit (keep changes):**
```bash
git reset --soft HEAD~1
```

**Undo last commit (discard changes):**
```bash
git reset --hard HEAD~1
```

**Revert a commit (create new commit that undoes):**
```bash
git revert <commit-hash>
```

---

### 8. What is git stash? When would you use it?

**Answer:**
`git stash` temporarily saves uncommitted changes so you can work on something else.

**Common commands:**
```bash
git stash                    # Save changes
git stash save "message"     # Save with description
git stash list               # List all stashes
git stash apply              # Apply latest stash (keep it)
git stash pop                # Apply and remove stash
git stash drop               # Delete a stash
git stash clear              # Delete all stashes
```

**Use cases:**
- Need to switch branches but have uncommitted changes
- Pull latest code without committing incomplete work
- Quick context switching

---

### 9. What is the difference between git reset, git revert, and git checkout?

**Answer:**

| Command | Purpose | History Change | Use Case |
|---------|---------|----------------|----------|
| `git reset` | Move HEAD pointer | Yes (rewrites) | Undo local commits |
| `git revert` | Create inverse commit | No (adds commit) | Undo pushed commits |
| `git checkout` | Switch branches/restore | No | Navigate or restore files |

**Examples:**
```bash
git reset --soft HEAD~1    # Undo commit, keep changes staged
git reset --mixed HEAD~1   # Undo commit, unstage changes
git reset --hard HEAD~1    # Undo commit, discard changes

git revert abc123          # Create new commit undoing abc123

git checkout main          # Switch to main branch
git checkout -- file.txt   # Restore file from last commit
```

---

## Branching & Merging

### 10. What is a branch in Git? Why do we use branches?

**Answer:**
A branch is an independent line of development. It's a pointer to a specific commit.

**Benefits:**
- Isolate feature development
- Multiple features in parallel
- Test without affecting main code
- Easy rollback
- Team collaboration

**Common branches:**
- `main/master` - Production code
- `develop` - Integration branch
- `feature/*` - New features
- `hotfix/*` - Emergency fixes
- `release/*` - Release preparation

---

### 11. What is the difference between git merge and git rebase?

**Answer:**

**Git Merge:**
```bash
git merge feature-branch
```
- Creates a merge commit
- Preserves complete history
- Non-destructive
- Can create complex history graph

**Git Rebase:**
```bash
git rebase main
```
- Rewrites commit history
- Creates linear history
- Cleaner history
- Never rebase public/shared branches

**Visual:**
```
Merge:
main:    A---B---C---F (merge commit)
              \     /
feature:       D---E

Rebase:
main:    A---B---C
                  \
feature:           D'---E'
```

---

### 12. What is a merge conflict? How do you resolve it?

**Answer:**
A merge conflict occurs when Git cannot automatically merge changes because the same lines were modified differently.

**Steps to resolve:**

1. **Identify conflicted files:**
```bash
git status
```

2. **Open conflicted file and look for markers:**
```
<<<<<<< HEAD
Your changes
=======
Incoming changes
>>>>>>> branch-name
```

3. **Manually edit the file:**
- Remove conflict markers
- Keep desired changes
- Combine both if needed

4. **Mark as resolved:**
```bash
git add <file>
git commit -m "Resolved merge conflict"
```

**Prevention:**
- Pull frequently
- Keep branches short-lived
- Communicate with team
- Use feature flags

---

### 13. What is Git branching strategy? Explain GitFlow.

**Answer:**

**GitFlow Branches:**
1. **main** - Production-ready code
2. **develop** - Integration branch
3. **feature/*** - New features (from develop)
4. **release/*** - Release prep (from develop)
5. **hotfix/*** - Production fixes (from main)

**Workflow:**
```bash
# Start new feature
git checkout develop
git checkout -b feature/new-feature

# Complete feature
git checkout develop
git merge feature/new-feature
git branch -d feature/new-feature

# Prepare release
git checkout -b release/1.0.0 develop
# Test and fix bugs
git checkout main
git merge release/1.0.0
git tag -a v1.0.0
git checkout develop
git merge release/1.0.0

# Hotfix
git checkout -b hotfix/critical-bug main
# Fix bug
git checkout main
git merge hotfix/critical-bug
git tag -a v1.0.1
git checkout develop
git merge hotfix/critical-bug
```

**Alternatives:**
- GitHub Flow (simpler)
- Trunk-Based Development
- GitLab Flow

---

## Git Workflow

### 14. How do you create a Pull Request workflow?

**Answer:**

**Steps:**
1. **Create feature branch:**
```bash
git checkout -b feature/new-feature
```

2. **Make changes and commit:**
```bash
git add .
git commit -m "Add new feature"
```

3. **Push to remote:**
```bash
git push origin feature/new-feature
```

4. **Create Pull Request on GitHub:**
- Navigate to repository
- Click "New Pull Request"
- Select source and target branches
- Add title and description
- Assign reviewers

5. **Code review process:**
- Reviewers add comments
- Make requested changes
- Push updates to same branch

6. **Merge PR:**
- Approve and merge
- Delete feature branch
- Pull latest main

---

### 15. What are Git hooks? Give examples.

**Answer:**
Git hooks are scripts that run automatically on certain Git events.

**Types:**
- **Client-side**: pre-commit, pre-push, post-checkout
- **Server-side**: pre-receive, post-receive, update

**Common uses:**
- **pre-commit**: Run linters, formatters, tests
- **pre-push**: Run test suite before pushing
- **commit-msg**: Validate commit message format
- **post-merge**: Install dependencies after merge

**Example pre-commit hook:**
```bash
#!/bin/sh
# .git/hooks/pre-commit

# Run tests
npm test
if [ $? -ne 0 ]; then
    echo "Tests failed, commit aborted"
    exit 1
fi

# Run linter
npm run lint
```

---

### 16. What is .gitignore? Why is it important?

**Answer:**
`.gitignore` specifies files that Git should ignore and not track.

**Common patterns:**
```
# Dependencies
node_modules/
vendor/

# Build outputs
target/
dist/
build/
*.jar

# Environment files
.env
.env.local

# IDE files
.vscode/
.idea/
*.iml

# OS files
.DS_Store
Thumbs.db

# Logs
*.log
logs/

# Temporary files
*.tmp
*.swp
```

**Importance:**
- Keeps repository clean
- Reduces repository size
- Prevents sensitive data commits
- Avoids conflicts on generated files

---

## Advanced Git Concepts

### 17. What is git cherry-pick?

**Answer:**
Cherry-pick applies specific commits from one branch to another.

**Use case:**
- Pick a bug fix from develop to hotfix
- Apply specific feature commits

**Command:**
```bash
git checkout main
git cherry-pick abc123
git cherry-pick abc123 def456 ghi789  # Multiple commits
```

**Example scenario:**
```
You fixed a critical bug in feature branch but need it in main immediately
instead of merging the entire feature branch.
```

---

### 18. What is git reflog?

**Answer:**
`git reflog` shows a log of all reference updates (HEAD movements).

**Use cases:**
- Recover deleted commits
- Find lost work after reset
- Undo mistakes

**Commands:**
```bash
git reflog                    # Show reference log
git reflog show HEAD          # Show HEAD movements
git reset --hard HEAD@{2}     # Go back 2 moves
```

**Example:**
```bash
# Accidentally deleted commits
git reset --hard HEAD~3

# Recover using reflog
git reflog
git reset --hard abc123
```

---

### 19. What is the difference between HEAD, working directory, and index?

**Answer:**

- **HEAD**: Pointer to current branch/commit
- **Working Directory**: Your actual files
- **Index (Staging Area)**: Snapshot of next commit

**Commands to see differences:**
```bash
git diff              # Working dir vs Index
git diff --staged     # Index vs HEAD
git diff HEAD         # Working dir vs HEAD
```

---

### 20. What is git tag? Types of tags?

**Answer:**
Tags mark specific points in history (usually releases).

**Types:**

1. **Lightweight tag:**
```bash
git tag v1.0.0
```

2. **Annotated tag (recommended):**
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
```

**Commands:**
```bash
git tag                        # List tags
git tag -l "v1.*"             # List matching tags
git show v1.0.0               # Show tag details
git push origin v1.0.0        # Push specific tag
git push origin --tags        # Push all tags
git tag -d v1.0.0             # Delete local tag
git push origin --delete v1.0.0  # Delete remote tag
```

---

### 21. What is git bisect?

**Answer:**
Binary search to find the commit that introduced a bug.

**Workflow:**
```bash
git bisect start
git bisect bad                 # Current commit is bad
git bisect good abc123         # Known good commit

# Git checks out middle commit
# Test it
git bisect good   # or bad

# Repeat until found
git bisect reset  # End bisect session
```

**Automated bisect:**
```bash
git bisect start HEAD abc123
git bisect run npm test
```

---

### 22. Explain git submodules.

**Answer:**
Submodules allow you to include other Git repositories within your repository.

**Commands:**
```bash
# Add submodule
git submodule add https://github.com/user/repo.git path/to/submodule

# Clone with submodules
git clone --recursive <url>

# Initialize submodules in existing clone
git submodule init
git submodule update

# Update submodules
git submodule update --remote
```

**Use cases:**
- Include shared libraries
- Manage dependencies
- Separate large components

---

## Troubleshooting & Scenarios

### 23. You committed sensitive data (password). How do you remove it?

**Answer:**

**If not pushed yet:**
```bash
git reset --soft HEAD~1
# Remove sensitive data
git add .
git commit -m "Fixed commit"
```

**If already pushed:**
```bash
# Use git filter-branch or BFG Repo-Cleaner
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch path/to/file" \
  --prune-empty --tag-name-filter cat -- --all

# Force push
git push origin --force --all
```

**Best practice:**
- Use .gitignore for sensitive files
- Use environment variables
- Rotate compromised credentials immediately

---

### 24. How do you squash commits?

**Answer:**
Combine multiple commits into one.

**Interactive rebase:**
```bash
git rebase -i HEAD~3  # Squash last 3 commits
```

**In editor:**
```
pick abc123 First commit
squash def456 Second commit
squash ghi789 Third commit
```

**During merge:**
```bash
git merge --squash feature-branch
git commit -m "Merged feature"
```

---

### 25. Your PR conflicts with main. How do you fix it?

**Answer:**

**Method 1: Merge main into feature**
```bash
git checkout feature-branch
git pull origin main
# Resolve conflicts
git add .
git commit -m "Resolved conflicts with main"
git push origin feature-branch
```

**Method 2: Rebase on main (cleaner)**
```bash
git checkout feature-branch
git pull origin main --rebase
# Resolve conflicts
git add .
git rebase --continue
git push origin feature-branch --force
```

---

### 26. How do you rename a branch?

**Answer:**

**Rename current branch:**
```bash
git branch -m new-name
```

**Rename another branch:**
```bash
git branch -m old-name new-name
```

**Update remote:**
```bash
git push origin -u new-name      # Push new branch
git push origin --delete old-name  # Delete old branch
```

---

### 27. How do you delete a branch locally and remotely?

**Answer:**

**Delete local branch:**
```bash
git branch -d branch-name      # Safe delete (merged only)
git branch -D branch-name      # Force delete
```

**Delete remote branch:**
```bash
git push origin --delete branch-name
# or
git push origin :branch-name
```

---

### 28. What is the difference between origin and upstream?

**Answer:**

- **origin**: Your fork or main remote repository
- **upstream**: Original repository (when you fork)

**Setup upstream:**
```bash
git remote add upstream https://github.com/original/repo.git
```

**Sync with upstream:**
```bash
git fetch upstream
git checkout main
git merge upstream/main
git push origin main
```

---

### 29. How do you optimize a large Git repository?

**Answer:**

**Strategies:**
1. **Clean up:**
```bash
git gc --aggressive --prune=now
```

2. **Remove large files from history:**
```bash
# BFG Repo-Cleaner
bfg --strip-blobs-bigger-than 50M
```

3. **Shallow clone:**
```bash
git clone --depth 1 <url>
```

4. **Git LFS for large files:**
```bash
git lfs install
git lfs track "*.psd"
```

5. **Sparse checkout** (checkout only specific folders)

---

### 30. Explain the difference between fast-forward and three-way merge.

**Answer:**

**Fast-forward merge:**
- Linear history
- No merge commit created
- Just moves branch pointer forward
```bash
git merge feature  # (fast-forward)
```

**Three-way merge:**
- Creates merge commit
- Combines divergent branches
- Shows merge point in history
```bash
git merge --no-ff feature  # Force merge commit
```

**Visual:**
```
Fast-forward:
main:     A---B---C---D---E
                       ↑
                    feature

Three-way:
main:     A---B---C-------F (merge commit)
                   \     /
feature:            D---E
```

---

## Quick Tips for Interviews

### What interviewers look for:
✅ Understanding of basic Git workflow
✅ Experience with branching strategies
✅ Ability to resolve conflicts
✅ Knowledge of collaboration (PR, code review)
✅ Troubleshooting skills
✅ Best practices awareness

### Practice commands to know by heart:
```bash
git init, git clone
git add, git commit, git push, git pull
git branch, git checkout, git merge
git status, git log, git diff
git stash, git reset, git revert
git fetch, git rebase
```

### Common mistakes to avoid in interviews:
❌ Confusing git pull and git fetch
❌ Not knowing when to use merge vs rebase
❌ Unable to explain merge conflicts
❌ Not knowing .gitignore purpose
❌ Mixing up reset, revert, and checkout

---

## Additional Resources

- Official Git Documentation: https://git-scm.com/doc
- Pro Git Book (Free): https://git-scm.com/book/en/v2
- Git Branching Interactive: https://learngitbranching.js.org/
- GitHub Learning Lab: https://lab.github.com/

---

**Good luck with your interview! 🚀**
