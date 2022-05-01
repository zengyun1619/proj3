# Build Your Own World Design Document

**Partner 1: Yangweiyi Li**

**Partner 2: Yun Zeng**

## Classes and Data Structures

## Algorithms
Invariant:
1. Each edge can only be connected to one component.
2. The corner(endpoints of an edge) is not connectable.
3. An edge is connectable in the first place only if it's at least one tile away from the border, and it has never been connected to any component.
4. If there is not enough room for an edge to connect a component, we mark the edge non-connectable.
5. When connecting a component to an edge, we replace the corresponding wall tiles on each side with floor tiles.


generateRoomAndHallways
1. getNextComponent
2. return a connectable component

connect

Termination Condition:
When the filled area exceeds half of the total area, stop adding more rooms or hallways to the tile word, and return it.


## Persistence
