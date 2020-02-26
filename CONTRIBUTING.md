# Contributing to 1M5

Anyone is welcome to contribute to 1M5. It is fully open source in the global public domain.
This document provides an overview of the process.
It draws from Bitcoin, Monero, and Bisq's successful contribution process.

If you're looking for somewhere to start contributing, check out
[good first issue](https://github.com/1m5/1m5/issues?q=is%3Aopen+is%3Aissue+label%3A"good+first+issue") list.

A good way to help is to test and report bugs. See
[How to Report Bugs Effectively (by Simon Tatham)](https://www.chiark.greenend.org.uk/~sgtatham/bugs.html)
if you want to help that way. Testing is invaluable in making a piece of software solid and usable.


## Communication Channels

Most communication about 1M5 happens in the 1m5_io team in [Keybase](https://keybase.io/team/1m5_io).

Install Keybase and enter "1m5_io" from the teams tab.
This is a closed team, which means the admins must approve a request to join.
This minimizes spam and trolls distracting developers.

Discussion about code changes happens in GitHub issues and pull requests.

Discussion about larger changes to the way 1M5 works happens in the issues for proposals [1m5/proposals](https://github.com/1m5/proposals/issues) repository.


## Categories

The primary categories for assisting are Growth, Dev, Ops, Support, and Admin.

### Growth

* Helps spread the benefits of using 1M5 acting as an ambassador publicly
* Identifies new areas where 1M5 could be helpful
* Works to bring in gifts and donations
* Looks for opportunities for collaboration with other missions/teams
* Builds and maintains web sites to provide information on the platform

### Dev

* Develops codebase including platform, desktop, common, cli, and api.

### Ops

* Manages nodes supporting infrastructure including seednodes
* Monitors node statistics for evaluating node health and identifying attacks
* Develops scripts as necessary to automate nodes

### Support

* Tests the codebase for bugs
* Represents end-users in usage of software using 1M5
* Troubleshoots codebase to determine where issues exist
* Works with Dev and Ops to ensure end-user issues gets resolved

### Security

* Provides penetration testing of 1M5 stack
* Identifies threats
* Determines best means to counter threats
* Creates dev and ops requirements for implementing threat counters

### Admin

* Facilitates communications within the community to drive consensus
* Works to mediate conflicts
* Documents results from consensus-derived decisions
* Approves/rejects budgets
* Allocates funds

## Contributor Workflow

All 1M5 contributors submit changes via pull requests. The workflow is as follows:

 - Fork the repository
 - Create a topic branch from the `master` branch
 - Commit patches
 - Squash redundant or unnecessary commits
 - Submit a pull request from your topic branch back to the `master` branch of the main repository
 - Make changes to the pull request if reviewers request them and __**request a re-review**__

Pull requests should be focused on a single change. Do not mix, for example, refactorings with a bug fix or
implementation of a new feature. This practice makes it easier for fellow contributors to review each pull
request on its merits and to give a clear ACK/NACK (see below).


## General guidelines

* Comments are encouraged.
* Tests would be nice to have if you're adding functionality.

Patches are preferably to be sent via a Github pull request. If that
can't be done, patches in "git format-patch" format can be sent
(eg, posted to fpaste.org with a long enough timeout and a link
posted to #1m5-dev on irc.freenode.net or sent to info@1m5.io).

Patches should be self contained. A good rule of thumb is to have
one patch per separate issue, feature, or logical change. Also, no
other changes, such as random whitespace changes, indentation,
or fixing typos, spelling, or wording, unless user visible.
Following the code style of the particular chunk of code you're
modifying is encouraged. Proper squashing should be done (eg, if
you're making a buggy patch, then a later patch to fix the bug,
both patches should be merged).

If you've made random unrelated changes (either because your editor
is annoying or you made them for other reasons), you can select
what changes go into the coming commit using git add -p, which
walks you through all the changes and asks whether or not to
include this particular change. This helps create clean patches
without any irrelevant changes. git diff will show you the changes
in your tree. git diff --cached will show what is currently staged
for commit. As you add hunks with git add -p, those hunks will
"move" from the git diff output to the git diff --cached output,
so you can see clearly what your commit is going to look like.


## Reviewing Pull Requests

1M5 follows the review workflow established by the Bitcoin Core project.
The following is adapted from the [Bitcoin Core contributor documentation](https://github.com/bitcoin/bitcoin/blob/master/CONTRIBUTING.md#peer-review):

Anyone may participate in peer review which is expressed by comments in the pull request.
Typically reviewers will review the code for obvious errors, as well as test out the patch set and opine on the
technical merits of the patch.
Project maintainers take into account the peer review when determining if there is consensus to merge a pull request
(remember that discussions may have been spread out over GitHub, mailing list and IRC discussions).
The following language is used within pull-request comments:

 - `ACK` means "I have tested the code and I agree it should be merged"
 - `NACK` means "I disagree this should be merged", and must be accompanied by sound technical justification. NACKs without accompanying reasoning may be disregarded
 - `utACK` means "I have not tested the code, but I have reviewed it and it looks OK, I agree it can be merged"
 - `Concept ACK` means "I agree in the general principle of this pull request"
 - `Nit` refers to trivial, often non-blocking issues

Please note that Pull Requests marked `NACK` and/or GitHub's `Change requested` are closed after 30 days if not addressed.


## Commits and pull requests

Commit messages should be sensible. That means a subject line that
describes the patch, with an optional longer body that gives details,
documentation, etc.

When submitting a pull request on Github, make sure your branch is
rebased. No merge commits nor stray commits from other people in
your submitted branch, please. You may be asked to rebase if there
are conflicts (even trivially resolvable ones).

PGP signing commits is strongly encouraged. That should explain why
the previous paragraph is here.


## Compensation

1M5 is not a company nor organization of any kind, only a mission. Contributions are only eligible for compensation
if they were allocated as part of the budget. Please include the desire for compensation in any proposal or if you would
rather keep that private, contact 1M5 admins at info@1m5.io.

For any work that was approved and merged into 1M5's `master` branch, you can request compensation in Prana tokens when
they are available. For those who consistently work on 1M5, an invitation to join a 1M5 team is possible resulting in
compensation with Aten tokens.


## Style and Coding Conventions

### Java Coding Conventions
Google provides a decent list of coding conventions [here](https://google.github.io/styleguide/javaguide.html).
In addition we recommend grouping your imports first by onemfive imports, then by standard java library imports, then static.

### Sign your commits with GPG

See https://github.com/blog/2144-gpg-signature-verification for background and
https://help.github.com/articles/signing-commits-with-gpg/ for instructions.

### Write well-formed commit messages

* Separate subject from body with a blank line
* Limit the subject line to 50 characters
* Use the imperative mood in the subject line
* Wrap the body at 72 characters
* Use the body to explain what and why vs how

### Use an editor that supports Editorconfig

The [.editorconfig](.editorconfig) settings in this repository ensure consistent management of whitespace, line endings and more.
Most modern editors support it natively or with plugin.

### Keep the git history clean

It's very important to keep the git history clear, light and easily browse-able. This means contributors must make sure
their pull requests include only meaningful commits (if they are redundant or were added after a review, they should be removed)
and no merge commits.

## See also

* Docs in the Desktop

