# CMP Plot Claimer Script

A scarpet script to automatically manage CMP plots ordered by region file.

## Commands

- `/plot claim <name> [color] [overwrite]`: Claims a region for a plot.
  - `name`: Name of the plot.
  - `color`: Color of the square drawn around the region. Default is `lime`, can be any minecraft glass color, and can be `none` if you don't want an outline.
  - `overwrite`: Add this argument if you want to overwrite an already claimed plot.
- `plot unclaim [confirm]`: Unclaims a region from a plot.
  - `confirm`: Include this argument to skip the confirmation message.
- `plot modify <name> tp <dimension> <location> <rotation>` and `plot modify <name> name <name>`: Modifies plot properties.
  - `dimension`: Dimension of the tp location.
  - `location`: Tp location.
  - `rotation`: Tp rotation.
- `plot tp <name>`: Teleports you to the plot <name>.
- `plot query`: Prints information about the current region.
- `plot list`: Lists all the plots.
- `plot delete <name>`: Deletes a plot.
  - `name`: Name of the plot.
- `plot join <name>`: Adds you to the list of participants in a plot.
  - `name`: Name of the plot.
  
