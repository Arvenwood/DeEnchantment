# DeEnchantment
 Turn enchanted items back into books! 

## Commands

- /deenchant
  - **Permission: deenchantment.use**
- /deenchant help
  - **Permission: deenchantment.help**
  - Shows the help menu.
- /deenchant reload
  - **Permission: deenchantment.reload**
  - Reloads the configuration.
- /deenchant one
  - **Permission: deenchantment.one**
  - Randomly selects one enchantment to be transferred onto the enchanted book.
  - The deenchanted item is destroyed.
- /deenchant all
  - **Permission: deenchantment.all**
  - Transfers all enchantments onto the enchanted book.
  - The deenchanted item is destroyed.

## Configuration
```
# Enchantments that can't be deenchanted.
blacklist=[
    "minecraft:mending"
]
```