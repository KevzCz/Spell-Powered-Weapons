# Spell Powered Weapons

Data-pack reference for authoring weapon conversions.

Two folders under `data/<your_namespace>/`:

- `spw_conversions/` — a fixed conversion. Matching items always get exactly this, no roll.
- `spw_rolls/` — a weighted roll. Matching items draw one outcome at random.

An authored conversion takes precedence over any roll. Folder names inside these directories carry no
meaning; files load recursively at any depth.

---

## Modes

| Mode | Physical | Elemental |
| --- | --- | --- |
| `split` | Reduced by `ratio` | A share of the weapon, plus scaling |
| `additive` | Untouched | Bonus on top, from scaling only |
| `full_elemental` | Zeroed | The whole weapon converts |
| `proportional` | Untouched | A share of damage actually dealt |
| `plain` | Untouched | None — the "no conversion" outcome |

Non-proportional entries produce:

```
elemental = (weapon_damage × ratio) + base + (spell_power × coefficient)
```

---

## File fields

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `mode` | string | `plain` | `split`, `additive`, `full_elemental`, `proportional`, `plain` |
| `weight` | int | `10` | Rolls only. Relative share of the draw, not a percentage |
| `verifiers` | list | `[]` | Items this applies to |
| `excludes` | list | `[]` | Items carved back out |
| `entries` | list | `[]` | The conversions themselves |
| `rarity` | string | `""` | Parsed and stored, but not currently used |
| `type` | string | — | Only `"override"`, which marks the file as an override |

## Entry fields

| Field | Type | Used by | Notes |
| --- | --- | --- | --- |
| `school` | string | split, additive, full_elemental | e.g. `spell_power:fire` |
| `ratio` | number/range | split | Share taken from the weapon |
| `coefficient` | number/range | split, additive, full_elemental | Multiplied by Spell Power |
| `base` | number/range | split, additive, full_elemental | Flat added amount |
| `share` | number/range | proportional | Share of damage dealt |
| `source_type` | string | proportional | Damage type to react to; omit for any |
| `output_type` | string | proportional | Damage type dealt |
| `source_name` | text component | proportional | `{"text":…}` or `{"translate":…}` |
| `output_name` | text component | proportional | Same |
| `output_icon` | string | proportional | Texture path for the tooltip glyph |

Any number field accepts a fixed value or a rolled range:

```jsonc
"coefficient": 0.35
"coefficient": { "min": 0.2, "max": 0.5 }
```

## Verifiers

`verifiers` and `excludes` accept item ids or tags, in short or long form:

```jsonc
"verifiers": [
    "minecraft:golden_sword",
    "#minecraft:swords",
    { "id": "minecraft:trident" },
    { "tag": "minecraft:axes" }
]
```

An item matches if any verifier matches and no exclude does. This mod ships
`#spell_powered_weapons:conversion_targets`, covering swords, axes, tridents, bows, crossbows, maces,
and the vanilla `enchantable/*` tags.

> The bundled example rolls use `"tag": "spw:conversion_targets"`, but the tag ships under
> `spell_powered_weapons:`. Use the full namespace in your own files. (`spw:` *is* correct for roll
> ids in overrides — a different thing.)

## Override fields

Filters which rolls an item may draw. Defines no conversions of its own.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `type` | string | — | Must be `"override"` |
| `verifiers` | list | `[]` | Items this applies to |
| `include` | list | `[]` | Roll patterns to permit |
| `exclude` | list | `[]` | Roll patterns to bar |
| `replace` | bool | `false` | `true` starts from nothing; only `include` is drawable |

Patterns accept `mode:full_elemental`, an exact roll id `spw:split/sword_minor`, or a prefix glob
`spw:split/*`. Roll ids come from the file's path under `spw_rolls/`, always in the `spw:` namespace.

---

## Examples

### Fixed conversion — `spw_conversions/flame_sword.json`

```jsonc
{
    "mode": "split",
    "verifiers": [ "minecraft:golden_sword" ],
    "rarity": "rare",
    "entries": [
        {
            "school": "spell_power:fire",
            "ratio": 0.40,
            "coefficient": 0.5,
            "base": 0.0
        }
    ]
}
```

### Rolled split — `spw_rolls/sword_minor.json`

```jsonc
{
    "mode": "split",
    "weight": 40,
    "verifiers": [ "#spell_powered_weapons:conversion_targets" ],
    "excludes": [ "minecraft:netherite_sword" ],
    "entries": [
        {
            "school": "spell_power:fire",
            "ratio":       { "min": 0.20, "max": 0.35 },
            "coefficient": { "min": 0.30, "max": 0.45 },
            "base":        { "min": 0.0,  "max": 1.0  }
        }
    ]
}
```

### Two schools — `spw_rolls/sword_dual.json`

Composition is authored, never rolled: two entries always yield two schools.

```jsonc
{
    "mode": "split",
    "weight": 4,
    "verifiers": [ "#spell_powered_weapons:conversion_targets" ],
    "entries": [
        {
            "school": "spell_power:fire",
            "ratio":       { "min": 0.18, "max": 0.28 },
            "coefficient": { "min": 0.25, "max": 0.40 }
        },
        {
            "school": "spell_power:frost",
            "ratio":       { "min": 0.12, "max": 0.22 },
            "coefficient": { "min": 0.20, "max": 0.35 }
        }
    ]
}
```

### Additive — `spw_rolls/sword_ember.json`

```jsonc
{
    "mode": "additive",
    "weight": 25,
    "verifiers": [ "#spell_powered_weapons:conversion_targets" ],
    "entries": [
        {
            "school": "spell_power:fire",
            "coefficient": { "min": 0.20, "max": 0.40 },
            "base":        { "min": 0.5,  "max": 2.0  }
        }
    ]
}
```

### Proportional / lifesteal — `spw_rolls/sword_leech.json`

```jsonc
{
    "mode": "proportional",
    "weight": 1,
    "verifiers": [ "#spell_powered_weapons:conversion_targets" ],
    "entries": [
        {
            "share": { "min": 0.10, "max": 0.25 },
            "source_type": "minecraft:player_attack",
            "output_type": "spell_power:soul",
            "source_name": { "text": "attack damage" },
            "output_name": { "translate": "attribute.name.spell_power.soul" }
        }
    ]
}
```

### Plain filler — `spw_rolls/default.json`

The "nothing happens" outcome. A high weight here is how you tune overall rarity.

```jsonc
{
    "weight": 89,
    "verifiers": [ "#spell_powered_weapons:conversion_targets" ]
}
```

### Override — `spw_rolls/override/wooden_tools.json`

```jsonc
{
    "type": "override",
    "verifiers": [ "minecraft:wooden_sword" ],
    "exclude": [
        "mode:full_elemental",
        "spw:split/sword_dual_school"
    ]
}
```

---

## Commands

`/reload` re-reads every file.

| Command | Does |
| --- | --- |
| `/spw roll simulate [samples]` | Draws the held item's pool N times, prints the distribution |
| `/spw info` | Shows the conversion on the held item |
| `/spw split <school> <ratio> <coefficient> [base]` | Applies one by hand |
| `/spw additive <school> <coefficient> [base]` | Applies one by hand |
| `/spw full_elemental <school> <coefficient> [base]` | Applies one by hand |
| `/spw proportional <share> of <source> as <output>` | Applies one by hand |
| `/spw remove <school>` / `/spw clear` | Removes entries |
| `/spw hide on\|off` | Hides the damage line without changing damage |
| `/spw suppress on\|off` | Cancels physical damage entirely |
| `/spw debug on\|off` | Echoes split-reduction diagnostics to chat |
