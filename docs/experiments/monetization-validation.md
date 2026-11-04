# Experiment: does a shared menu reach anyone?

**Status:** pre-registered, not started. The four-week clock has not begun because
the distribution channel is not yet named. Nothing in this file may be edited after
the link is posted; results are appended, never rewritten.

**Owner and reconciliation:** Micah Silverman, every Monday while the window is
open.

## What this measures, and what it does not

This experiment measures **distribution and activation**: can one shared menu reach
strangers, and will a stranger act on it? It does **not** measure willingness to
pay. Passing this bar justifies designing the money surface (pricing page, deposit
path, refund terms, weekly ledger). It does not prove anyone will pay, and it must
not be reported as if it does.

The instrument is the `menu_event` table, written by the application for one share
token at a time. Every rate below is reported with its raw counts, because the
denominators are small enough that a bare percentage misleads.

## Setup, fixed before posting

| Item | Value |
| --- | --- |
| Link | One share token, created by the owner from **Manage → Share** |
| Channel | **TBD — must be filled in below before the link is posted** |
| Post date | **TBD** — the 28-day window starts on this date |
| Audience | One named community thread or group chat, posted once |
| Follow-up | One reminder post in the same thread, allowed, recorded below |

> Channel: _to be named_
>
> Posted on: _to be recorded_
>
> Reminder posted on: _optional, to be recorded_

A link posted to a channel with no plausible audience (a private note to self, a
test message) does not start the clock.

## Hypothesis

One person's real menu, shared as an anonymous page, produces enough traffic for a
stranger to copy a dish or start an order within four weeks.

## Bars

**Reach floor — 20 `share_view` events.** Below 20, the window reports
**insufficient reach**. That is a valid, reportable result: it says the channel did
not deliver, not that the product failed. No metric is interpreted below the floor.

**Activation bar — with the floor met, at least one non-owner account copies the
menu or starts an order.** Concretely, at least one of:

- ≥ 1 `menu_copied` event on the token, or
- ≥ 1 `order_started` event on the token,

where the `owner_sub` on the event is not the sharer's subject. One is enough for
"the loop works"; two or more from different events is "the loop repeats".

**Sequencing bar — copying precedes ordering.** In the funnel, `menu_copied` or
`order_started` must come from a token that also produced a `share_view` before it.
The bar is measured per link, not per person: a visitor is anonymous when the page
loads, so a `share_view` carries only the token, while a copy or order carries the
acting account's subject. A copy or order on a token with no earlier `share_view` on
that token is not counted. This is the check that separates a real visitor from the
owner testing their own link.

## Stop rule

The window ends 28 days after the post date, at midnight in the owner's local time.
It is not extended because the numbers are close, and the bars are not lowered
after the fact. Two exceptions, both recorded before they are used:

1. **Late post.** If the channel posts the link more than 7 days after this file is
   written, the window starts on the post date, not the file date.
2. **Broken week.** If the public site returns 5xx or the share page cannot be
   opened for more than one hour, the window pauses on that day and resumes when
   the site is healthy; the pause is recorded.

At the end of the window, the result is written once, in the Results section, and
the decision is one of: **insufficient reach**, **loop works**, or **loop repeats**.
No outcome triggers building the money surface on its own; that is a separate
design with its own pre-registered bar.

## Weekly reconciliation

Run against the production database and paste the raw numbers into the log.

```sql
-- the funnel for the one link
select kind, count(*) as events
from menu_event
where token = :token
group by kind
order by kind;

-- distinct orders that reached completion through this token
select count(distinct e.order_id) as completed_orders
from menu_event e
join menu_order o on o.id = e.order_id
where e.kind = 'order_completed'
  and e.token = :token;

-- the accounts this link is attributed to
select distinct owner_sub
from menu_event
where token = :token
  and kind in ('menu_copied', 'order_started');
```

Recorded values, one row per week:

| Week ending | `share_view` | `menu_copied` | `order_started` | `order_completed` | Notes |
| --- | --- | --- | --- | --- | --- |
| _pending_ | | | | | |

## Data retention

`menu_event` is deliberately unbounded for the length of the experiment. After the
window closes and the result is written, the token's rows are kept as evidence for
one year and then pruned:

```sql
delete from menu_event where occurred_at < now() - interval '1 year';
```

The index on `(token, kind)` keeps the weekly funnel queries cheap; the one-year
prune is a one-off scan by `occurred_at`.

## Results

_Not started. Append the final counts, the outcome, and one paragraph on what was
learned. Do not edit the sections above._
