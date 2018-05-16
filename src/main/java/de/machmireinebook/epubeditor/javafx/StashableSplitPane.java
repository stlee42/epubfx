package de.machmireinebook.epubeditor.javafx;

import java.util.Arrays;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

/**
 * Created by Michail Jungierek
 */
public class StashableSplitPane extends SplitPane
{
    private static final Logger logger = Logger.getLogger(StashableSplitPane.class.getName());
    private Node[] originalItems;
    private boolean[] visibles;
    private double[] proportions;

    public StashableSplitPane() {
        super();
        visibles = null;
        originalItems = null;
        proportions = null;
    }

    private void init() {
        originalItems = getItems().toArray(new Node[0]);
        visibles = new boolean[originalItems.length];
        proportions = new double[originalItems.length];
        Arrays.fill(visibles, true);
        Arrays.fill(proportions, 0.0);
    }

    /**
     * Hides a child node of this pane.
     * <p>
     * If the child node is already hidden, calling this function has no effect.
     *
     * @param index
     *          the index of the child node to hide.
     * @throws IllegalArgumentException
     *           if the index is invalid.
     */
    public final void hideItem(int index) {
        logger.info("Hide item " + index);
        if (visibles == null) {
            init();
        }
        if ((index < 0) || (index >= visibles.length)) {
            throw new IllegalArgumentException("Invalid index of item.");
        }
        if (! visibles[index]) { // the node has already been hidden
            return;
        }
        final ObservableList<Node> items = getItems();
        if (items.size() <= 1) {
            logger.warning("All other children panes were hidden, so ignore this action.");
            return;
        }
        final Node node = originalItems[index];
        // find the node in the item list
        final int pos = findNode(items, node);
        if (pos >= items.size()) {
            logger.severe("Try to hide a non-exist node.");
            return;
        }
        // member the proportion that the node occupies
        double[] positions = getDividerPositions();
        logger.fine("pos = " + pos);
        proportions[index] = getProportion(positions, pos);
        logger.fine("proportions[index] = " + proportions[index]);
        // remove the node from the item list
        items.remove(pos);
        // set the new divider positions
        double[] newPositions = getPositionsAfterRemoving(positions, pos);
        setDividerPositions(newPositions);
        visibles[index] = false;
    }

    private int findNode(ObservableList<Node> items, Node node) {
        final int n = items.size();
        for (int i = 0; i < n; ++i) {
            if (items.get(i) == node) {
                return i;
            }
        }
        return n;
    }

    /**
     * Shows a child node of this pane.
     * <p>
     * If the child node is already shown, calling this function has no effect.
     *
     * @param index
     *          the index of the child node to show.
     * @throws IllegalArgumentException
     *           if the index is invalid.
     */
    public void showItem(int index) {
        logger.fine("Show item " + index);
        if (visibles == null) {
            init();
        }
        if ((index < 0) || (index >= visibles.length)) {
            throw new IllegalArgumentException("Invalid index of item.");
        }
        if (visibles[index]) { // the node has already been shown
            return;
        }
        final Node node = originalItems[index];
/*        if (! (node instanceof Pane)) {
            throw new IllegalArgumentException("Only pane node can be hidden.");
        }*/
        // find the insertion position of the node
        ObservableList<Node> items = getItems();
        int pos = findInsertPosition(items, node);
        logger.fine("pos = " + pos);
        double[] positions = getDividerPositions();
        // insert the node to the item list, if its not yet included e.g. by fxml
        if (!items.contains(node))
        {
            items.add(pos, node);
        }
        // restore the original proportion occupied by the node
        double[] newPositions = getPositionsAfterInsertion(positions, pos,
                proportions[index]);
        logger.fine("proportions[index] = " + proportions[index]);
        setDividerPositions(newPositions);

        visibles[index] = true;
    }

    private int findInsertPosition(ObservableList<Node> items, Node node) {
        final int n = items.size();
        int i = 0;
        for (; i < n; ++i) {
            if (isBefore(node, items.get(i))) {
                return i;
            }
        }
        return n;
    }

    private double getProportion(double[] positions, int pos) {
        final int n = positions.length;
        if (n == 0) {
            return 1.0;
        }
        if (pos == 0) { // this is the first child
            return positions[0];
        } else if (pos == n) { // this is the last child
            return 1.0 - positions[n - 1];
        } else { // this is the middle child
            return positions[pos] - positions[pos - 1];
        }
    }

    private double[] getPositionsAfterRemoving(double[] positions, int pos) {
        final int n = positions.length;
        if (n == 1) {
            return new double[0];
        }
        final double[] result = new double[n - 1];
        if (pos == 0) { // remove the first child
            // all spaces are allocated to its right sibling
            System.arraycopy(positions, 1, result, 0, n - 1);
        } else if (pos == n) { // remove the last child
            // all spaces are allocated to its left sibling
            System.arraycopy(positions, 0, result, 0, n - 1);
        } else { // remove the middle child
            // the spaces are allocated to its left and right sibling equally
            System.arraycopy(positions, 0, result, 0, pos - 1);
            System.arraycopy(positions, pos + 1, result, pos, n - pos - 1);
            result[pos - 1] = (positions[pos - 1] + positions[pos]) / 2;
        }
        return result;
    }

    private double[] getPositionsAfterInsertion(double[] positions, int pos,
                                                double proportion) {
        final int n = positions.length;
        final double[] result = new double[n + 1];
        if (pos == 0) { // insert as the first child
            // all spaces are collected from its right sibling
            result[0] = proportion;
            System.arraycopy(positions, 0, result, 1, n);
        } else if (pos == (n + 1)) { // insert as the last child
            // all spaces are collected from its left sibling
            System.arraycopy(positions, 0, result, 0, n);
            result[n] = 1.0 - proportion;
        } else { // insert as the middle child
            // the spaces are collected from its left and right sibling equally
            System.arraycopy(positions, 0, result, 0, pos - 1);
            System.arraycopy(positions, pos, result, pos + 1, n - pos);
            result[pos - 1] = positions[pos - 1] - (proportion / 2);
            result[pos] = positions[pos - 1] + (proportion / 2);
        }
        return result;
    }

    private boolean isBefore(Node node1, Node node2) {
        for (Node originalItem : originalItems)
        {
            if (originalItem == node1)
            {
                return true;
            }
            else if (originalItem == node2)
            {
                return false;
            }
        }
        return false;
    }

    /**
     * Sets the visibility of a child pane.
     *
     * @param index
     *          the index of a child pane.
     * @param visible
     *          indicates whether the child pane is to be visible.
     * @throws IllegalArgumentException
     *           if the index is invalid.
     */
    public final void setVisibility(int index, boolean visible) {
        logger.fine("Set item " + index + " visible to " + visible);
        if (visible) {
            showItem(index);
        } else {
            hideItem(index);
        }
    }
}
