package org.flymine.objectstore.query;

/*
 * Copyright (C) 2002-2003 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.List;
import java.util.Arrays;

import org.flymine.model.FlyMineBusinessObject;
import org.flymine.util.Util;

/**
 * Constrain whether a QueryClass is equal/not equal to another
 * QueryClass or an example of an object belonging to a
 * QueryClass. Note: QueryClass = QueryClass makes no sense, but is
 * allowed.
 *
 * @author Richard Smith
 * @author Mark Woodbridge
 * @author Andrew Varley
 * @author Matthew Wakeling
 */
public class ClassConstraint extends Constraint
{
    protected QueryClass qc1, qc2;
    protected FlyMineBusinessObject obj;

    /**
     * Construct ClassConstraint
     *
     * @param qc1 first QueryClass for comparison
     * @param op define EQUALS or NOT_EQUALS
     * @param qc2 second QueryClass for comparison
     */
    public ClassConstraint(QueryClass qc1, ConstraintOp op, QueryClass qc2) {
        if (qc1 == null) {
            throw new NullPointerException("qc1 cannot be null");
        }

        if (op == null) {
            throw new NullPointerException("op cannot be null");
        }
        
        if (!VALID_OPS.contains(op)) {
            throw new IllegalArgumentException("op cannot be " + op);
        }

        if (qc2 == null) {
            throw new NullPointerException("qc2 cannot be null");
        }
        
        if (!(qc1.getType().isAssignableFrom(qc2.getType())
              || qc2.getType().isAssignableFrom(qc1.getType()))) {
            throw new IllegalArgumentException("Invalid constraint: "
                                               + qc1.getType()
                                               + " " + op
                                               + " " + qc2.getType());
        }
        
        this.qc1 = qc1;
        this.op = op;
        this.qc2 = qc2;
    }

    /**
     * Construct ClassConstraint
     *
     * @param qc QueryClass for comparison
     * @param op define EQUALS or NOT_EQUALS
     * @param obj example object
     */
    public ClassConstraint(QueryClass qc, ConstraintOp op, FlyMineBusinessObject obj) {
        if (qc == null) {
            throw new NullPointerException("qc cannot be null");
        }

        if (op == null) {
            throw new NullPointerException("op cannot be null");
        }

        if (!VALID_OPS.contains(op)) {
            throw new NullPointerException("op cannot be " + op);
        }

        if (obj == null) {
            throw new NullPointerException("obj cannot be null");
        }

        if (!(qc.getType().isAssignableFrom(obj.getClass()))) {
              throw new IllegalArgumentException("Invalid constraint: "
                                               + qc.getType()
                                               + " " + op
                                               + " " + obj.getClass());
        }
        
        this.qc1 = qc;
        this.op = op;
        this.obj = obj;
    }

    /**
     * Returns the QueryClass argument 1
     *
     * @return QueryClass arg1
     */
    public QueryClass getArg1() {
        return qc1;
    }

    /**
     * Returns the QueryClass argument 2
     *
     * @return QueryClass arg2
     */
    public QueryClass getArg2QueryClass() {
        return qc2;
    }

    /**
     * Returns the Object argument 2
     *
     * @return FlyMineBusinessObject arg2
     */
    public FlyMineBusinessObject getArg2Object() {
        return obj;
    }

    /**
     * Tests whether two ClassConstraints are equal.
     *
     * @param o the object to compare with
     * @return true if objects are equal
     */
    public boolean equals(Object o) {
        if (o instanceof ClassConstraint) {
            ClassConstraint cc = (ClassConstraint) o;
            return  qc1.equals(cc.qc1)
                && op == cc.op
                && Util.equals(cc.qc2, qc2)
                && Util.equals(cc.obj, obj);
        }
        return false;
    }

    /**
     * Get the hashCode for this object
     *
     * @return the hashCode
     */
    public int hashCode() {
        return qc1.hashCode()
            + 3 * op.hashCode()
            + 5 * Util.hashCode(qc2)
            + 7 * Util.hashCode(obj);
    }

    protected static final List VALID_OPS = Arrays.asList(new ConstraintOp[] {ConstraintOp.EQUALS,
        ConstraintOp.NOT_EQUALS});
}
