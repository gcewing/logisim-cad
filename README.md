LogisimE
========

A fork of logisim-evolution.

Changes in this version of logisim
----------------------------------

### Improved snapping of text objects in the appearance editor

When dragging a text object while holding down the Control key, the point on
the object that is snapped to the grid now depends on the alignment properties of
the text. This makes it easier to position the text nicely in relation to
wire attachment points and other features of your circuit symbol.

### Finer snapping grid

The snapping grid in the appearance editor for all objects except ports
and the origin marker is half the size of the wire grid. This makes it
easy, for example, to line up an inversion circle with a wire properly.