# Photographic Pack — provenance and licence record

Face It ships two independent wallpaper packs. This document is the record for
the second one, and it exists because that pack contains **other people's work**.

* **153 wallpapers**, all inside `assets/photos/`. There is no second pack: the
  drawn artwork collection that used to ship alongside these was removed on
  instruction, along with its object renders and material tiles.
* **148 of them are real photographs** under free licences, each carrying its
  creator, its licence and a link to its source page.
* **5 are images generated for this application**, marked `synthetic: true` in the
  index. They are ours, dedicated to the public domain, and the index says so
  rather than dressing them up as photographs.

The distinction matters and the app does not blur it: the gallery shows each
photograph's credit on its tile, and the index distinguishes a photographer's work
from a generated image so the two can never be conflated.

**Generated images in the pack**

| Id | Category | What it is |
|---|---|---|
| `generated-gold-script-monogram` | diamonds | rose-gold cursive monogram on engraved black panels |
| `generated-black-diamond-macro` | diamonds | black diamond and brilliant cuts on brushed gunmetal |
| `generated-fluorite-cathedral` | minerals | backlit blue-green fluorite cluster |
| `generated-damascus-steel-macro` | metalwork | forged Damascus-pattern steel |
| `generated-turbine-hall` | ironwork | cast-iron turbine hall in raking light |

These exist because the free-licence pool does not contain the register the pack
was asked for — the hard-lit, high-contrast, rendered look. They are the only
images in the app that were not photographed by someone else, and they are
labelled as such.

## What is in the pack

The subject matter was changed by the product owner: **made things, minerals and
the spaces where metal is worked**, not animals and not landscapes. The animal and
nature categories that opened this pack were removed outright rather than left in
at lower priority, because a wallpaper gallery that still shows kittens has not
changed direction.

| Category | Count | What it covers |
|---|---|---|
| minerals | 36 | amethyst, fluorite, pyrite, malachite, azurite, quartz, obsidian, tourmaline, topaz, gypsum, halite, sulfur, smithsonite, geodes, agate slices |
| technology | 32 | circuit boards, computer hardware, server rooms, laboratory equipment, graphics cards, solar and wind power, telescopes, printers |
| luxury | 17 | watches, leather, silk, marble, crystal, still life |
| ironwork | 16 | turbine halls, power station interiors, gantry cranes, stock sheds, machine halls, industrial spaces |
| fashion | 15 | shoes, bags, hats, scarves, knitwear, lace, tweed, linen |
| bracelets | 12 | bangles, charm bracelets, pearl strands, gold chains, tiara, filigree, ruby and gemstone rings |
| diamonds | 13 | brilliant cuts, solitaire rings, rough stones, gemstone macro |
| metalwork | 12 | forged iron, rivets, wrought iron railings, anvils, steel trusses, iron bridges, forge fire |
| **total** | **153** | 148 photographs by 120 photographers and institutions, plus 5 generated images |

Total on-disk size: **52.2 MB**, masters at 1920 px long edge plus 640 px
thumbnails. A 2500 px master is right for print and wrong for an APK — Android
ships the JPEG unchanged, so the pack is re-encoded to a size a phone screen can
show. The original dimensions are kept in the index as `sourceWidth` /
`sourceHeight` so the provenance record stays truthful and the resize is recorded
rather than implied.

## Licences

| Licence | Count | Obligation |
|---|---|---|
| CC0 / public domain | 23 | none |
| CC BY | 43 | credit the creator, link the licence |
| CC BY-SA | 87 | credit the creator, link the licence, share derivatives alike |
| **total** | **153** | (5 of the CC0 entries are generated images) |

**87 of the 153 are share-alike.** That is a deliberate, recorded decision, not an
oversight:

* Shipping the photographs unmodified inside the app is permitted by CC BY-SA.
  The app displays the credit and links the licence, which is what the licence
  asks for.
* The obligation attaches to *modified* versions of those photographs. Face It
  does not modify them — the composed wallpaper places the photograph over a
  blurred enlargement of itself and adds a light scrim, which is a new composite
  work, while the photographs themselves are redistributed as they are.
* If the project ever needs to redistribute a *modified* variant of one of these
  images, that variant must be shared alike. The per-entry `shareAlike` flag in
  `photo_index.json` is what makes that auditable, and the app surfaces it as a
  `BY-SA` badge on the tile rather than hiding it.

A permissive-only alternative was considered and rejected: filtering to CC0 and
CC BY alone gives 61 photographs. The decision is reversible —
`python3 tools/fetch_photo_pack.py --only <category>` re-fetches, and the licence
preference order in that script already takes CC0 first.

## What is deliberately *not* in the pack

* **"Famous" photographs.** Well-known images are almost always under full
  copyright and bundling one in an APK is infringement regardless of how it was
  obtained. Only free licences are used.
* **Non-commercial and no-derivatives licences.** `CC BY-NC`, `CC BY-ND`, `GFDL`
  and all-rights-reserved files are refused by the pipeline and re-checked by the
  app at read time.
* **Animals and landscapes.** Removed on instruction. No kittens, no forests, no
  mountains, no beaches.
* **Drawn artwork.** The 100 vector-derived wallpapers, 10 object renders and 8
  material tiles were deleted, along with `artwork_index.json` and the code that
  read them. Everything the app ships as a wallpaper is now a photograph or a
  generated render.
* **Pictures that are not photographs, and photographs of the wrong thing.** A
  licence-clean scan of an oil painting, a watercolour, a museum diorama, a
  taxidermy mount, a satellite composite, a CGI product render or a sculpture is
  not a wallpaper. Nearly fifty such files were caught during review and removed,
  including some memorable search failures:

  | Query | What actually came back |
  |---|---|
  | `puppy` | a Jeff Koons flower sculpture in Bilbao, twice |
  | `silk tie` | three photographs of surgical stitches |
  | `emerald crystal` | a container ship called *Chrystal Emerald* |
  | `ruby crystal` | a group of people including one named Ruby and one named Crystal |
  | `geode` | an AMD Geode processor |
  | `malachite` | a butterfly, because the species is called the malachite |
  | `opal` | Opal Pool, Yellowstone, and Opal, Virginia |
  | `linen texture` | a coat of arms |
  | `silversmith` | a 17th-century engraving of a silversmiths' festival |
  | `diamond` | Diamond Street in San Francisco |

  Every one of those passed the numeric quality gates. Entropy, edge density and
  sharpness measure whether a photograph is technically sound; they cannot tell
  you what it is a photograph *of*. That check is done by eye, in contact sheets,
  and it is the reason this document has a table rather than a promise.

## How the pack was built

```
python3 tools/fetch_photo_pack.py --plan                    # list candidates
python3 tools/fetch_photo_pack.py --only minerals --limit 26 # fetch one category
python3 tools/fetch_photo_pack.py --optimise                # re-encode for phones
python3 tools/fetch_photo_pack.py --verify                  # hash + attribution check
```

Source is the Wikimedia Commons API (`generator=search`, `gsrnamespace=6`),
asking for originals rather than thumbnails, with `Retry-After` backoff because
Commons rate-limits large batches with HTTP 429. Openverse was tried first and
abandoned: it serves 1024 px derivatives, which is not enough for a 1440 px
wallpaper.

Acceptance gates, all enforced in code:

| Gate | Threshold | Why |
|---|---|---|
| resolution | ≥ 1600 × 900 | below this the composed wallpaper visibly softens |
| aspect | between 1:3 and 3:1 | extreme panoramas cannot be composed |
| entropy | ≥ 5.2 bits | rejects flat gradients and blank skies |
| edge density | ≥ 0.012 | rejects fog, haze and empty frames |
| sharpness | ≥ 0.055 | rejects out-of-focus uploads |
| artwork filter | title and Commons categories | keeps photography, rejects art |
| refusal terms | titles and subjects | keeps the subject on-brief |
| sha256 | unique | no file appears twice |
| perceptual hash | hamming ≤ 10 rejected | catches the same bangle returned by four queries |
| attribution | creator, licence URL, source page | incomplete records never ship |
| orphans | none | a file with no index entry still ships in the APK |

## Attribution surface

The credit travels with the photograph everywhere it is used:

* the gallery tile shows `creator · LICENCE`, with a `BY-SA` badge when the
  licence is share-alike;
* the accessibility description reads *"title by creator, licence photograph"*;
* applying a wallpaper confirms with *"subject applied — photo by creator"*;
* `photo_index.json` holds the full record: `title`, `creator`, `creatorUrl`,
  `licence`, `licenceVersion`, `licenceUrl`, `source`, `sha256` and the quality
  measurements the file passed.

## What the audit enforces

`tools/verify_catalogue.py` audits the photo pack on every CI run and fails the
build on: a missing or duplicate id, an unusable licence, an incomplete credit, a
missing file, a size or hash that does not match the index, an image larger than
a phone needs, **an orphaned file with no index entry**, a generated image that is
not marked as generated or not disclosed in this document, a theme naming a
wallpaper that is not in the pack, a category with fewer than five items, a pack
covering fewer than three categories, a Kotlin reader that has drifted from this
schema, and this document disagreeing with the pack. That last check is why the numbers above are not allowed to rot.
