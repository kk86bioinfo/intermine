package org.flymine.metadata;

/*
 * Copyright (C) 2002-2003 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public class ClassDescriptorTest extends TestCase
{
    private static final Set EMPTY_SET = Collections.EMPTY_SET;

    public ClassDescriptorTest(String arg) {
        super(arg);
    }

    public void testConstructNameNull() {
        try {
            ClassDescriptor cld = new ClassDescriptor("", null, false,
                                                      new HashSet(), new HashSet(), new HashSet());
            fail("Expected: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

    }

    public void testSetModel() throws Exception {
        ClassDescriptor cld = new ClassDescriptor("Class1", null, false,
                                                  new HashSet(), new HashSet(), new HashSet());
        try {
            Model model1 = new Model("model1", Collections.singleton(cld));
        } catch (IllegalStateException e) {
            fail("Model should have been set correctly");
        }

        try {
            Model model2 = new Model("model2", Collections.singleton(cld));
            fail("Model already set, expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
    }

    public void testInterfaceDescriptors() throws Exception {
        ClassDescriptor int1 = new ClassDescriptor("Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor int2 = new ClassDescriptor("Interface2", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("Class1", "Interface1 Interface2", false,
                                                  new HashSet(), new HashSet(), new HashSet());

        Set clds = new HashSet(Arrays.asList(new Object[] {int1, int2, cld}));
        Model model = new Model("test", clds);

        Set interfaces = new HashSet(Arrays.asList(new Object[] {int1, int2}));
        assertEquals(interfaces, cld.getSuperDescriptors());

    }

    public void testImplementsNotExists() throws Exception {
        // construct class implementing Interface3 which does not exist
        ClassDescriptor int1 = new ClassDescriptor("Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor int2 = new ClassDescriptor("Interface2", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("Class1", "Interface1 Interface3", false,
                                                  new HashSet(), new HashSet(), new HashSet());

        Set clds = new HashSet(Arrays.asList(new Object[] {int1, int2, cld}));
        try {
            Model model = new Model("test", clds);
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testInterfaceNotImplemented() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", Collections.singleton(cld1));
        assertTrue(cld1.getSubDescriptors().size() == 0);
    }

    public void testSuperClassExists() throws Exception {
        ClassDescriptor superCld = new ClassDescriptor("superCld", null, false,
                                                       new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("cld", "superCld", false,  new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("test", new HashSet(Arrays.asList(new Object[] {cld, superCld})));
        assertEquals(superCld, cld.getSuperclassDescriptor());
    }

    public void testSuperClassNotExists() throws Exception {
        ClassDescriptor superCld = new ClassDescriptor("superCld", null, false,
                                                       new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("cld", "anotherCld", false,  new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("test", new HashSet(Arrays.asList(new Object[] {cld, superCld})));
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testSuperClassWrongType() throws Exception {
        // test where superclass is not an interface, subclass is
        ClassDescriptor superCld = new ClassDescriptor("superCld", null, false,
                                                       new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor interface1 = new ClassDescriptor("interface1", "superCld", true,  new HashSet(), new HashSet(), new HashSet());
        try {
            Model model2 = new Model("test", new HashSet(Arrays.asList(new Object[] {interface1, superCld})));
            fail("Expected MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultipleSuperClass() throws Exception {
        ClassDescriptor superCld1 = new ClassDescriptor("superCld1", null, false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor superCld2 = new ClassDescriptor("superCld2", null, false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld = new ClassDescriptor("cld", "superCld1 superCld2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("test", new HashSet(Arrays.asList(new Object[] {cld, superCld1, superCld2})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }
        
    public void testFieldDescriptorByName() throws Exception {
        Set attributes = getAttributes();
        Set refs = getReferences();
        Set cols = getCollections();
        ClassDescriptor cld = new ClassDescriptor("Class1", null, false,
                                                  attributes, refs, cols);
        assertNotNull(cld.getFieldDescriptorByName("atd1"));
        assertNotNull(cld.getFieldDescriptorByName("atd2"));
        assertNotNull(cld.getFieldDescriptorByName("rfd1"));
        assertNotNull(cld.getFieldDescriptorByName("rfd2"));
        assertNotNull(cld.getFieldDescriptorByName("cld1"));
        assertNotNull(cld.getFieldDescriptorByName("cld2"));
    }

    public void testGetAllAttributeDescriptors() throws Exception {
        // three superclass levels with one attribute each, getAllAttributeDescriptors on cld3 should return all 3
        AttributeDescriptor atb1 = new AttributeDescriptor("att1", false, "String");
        ClassDescriptor cld1 = new ClassDescriptor("Class1", null, false, Collections.singleton(atb1), new HashSet(), new HashSet());
        AttributeDescriptor atb2 = new AttributeDescriptor("att2", false, "String");
        ClassDescriptor cld2 = new ClassDescriptor("Class2", "Class1", false, Collections.singleton(atb2), new HashSet(), new HashSet());
        AttributeDescriptor atb3 = new AttributeDescriptor("att3", false, "String");
        ClassDescriptor cld3 = new ClassDescriptor("Class3", "Class2", false, Collections.singleton(atb3), new HashSet(), new HashSet());

        Model model = new Model("test", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));

        FieldDescriptor id = cld3.getFieldDescriptorByName("id");
        Set atts = new HashSet(Arrays.asList(new Object[] {atb3, atb2, atb1, id}));

        assertEquals(atts, cld3.getAllAttributeDescriptors());
    }

    public void testGetPkFieldDescriptors() throws Exception {
        AttributeDescriptor atb1 = new AttributeDescriptor("att1", false, "String");
        AttributeDescriptor atb2 = new AttributeDescriptor("att2", true, "String");
        Set atts = new HashSet(Arrays.asList(new Object[] {atb1, atb2}));
        ReferenceDescriptor rfd1 = new ReferenceDescriptor("rfd1", false, "Class2", null);
        ReferenceDescriptor rfd2 = new ReferenceDescriptor("rfd2", true, "Class2", null);
        Set refs = new HashSet(Arrays.asList(new Object[] {rfd1, rfd2}));
        CollectionDescriptor cod1 = new CollectionDescriptor("cld1", false, "Class2", null, false);
        CollectionDescriptor cod2 = new CollectionDescriptor("cld2", true, "Class2", null, false);
        Set cols = new HashSet(Arrays.asList(new Object[] {cod1, cod2}));


        ClassDescriptor cld1 = new ClassDescriptor("Class1", null, false, atts, refs, cols);
        ClassDescriptor cld2 = new ClassDescriptor("Class2", null, false, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("test", new HashSet(Arrays.asList(new Object[] {cld1, cld2})));

        // six fields in class, three of them are pks
        Set pks = new HashSet(Arrays.asList(new Object[] {atb2, rfd2, cod2}));

        assertEquals(pks, cld1.getPkFieldDescriptors());
    }

    public void testGetPkFieldDescriptorsSuper() throws Exception {
        AttributeDescriptor atb1 = new AttributeDescriptor("att1", true, "String");
        AttributeDescriptor atb2 = new AttributeDescriptor("att2", false, "String");
        ClassDescriptor cld1 = new ClassDescriptor("Class1", null, false, new HashSet(Arrays.asList(new Object[] {atb1, atb2})),
                                                   new HashSet(), new HashSet());
        AttributeDescriptor atb3 = new AttributeDescriptor("att3", true, "String");
        AttributeDescriptor atb4 = new AttributeDescriptor("att4", false, "String");
        ClassDescriptor cld2 = new ClassDescriptor("Class2", "Class1", false, new HashSet(Arrays.asList(new Object[] {atb3, atb4})),
                                                   new HashSet(), new HashSet());
        AttributeDescriptor atb5 = new AttributeDescriptor("att5", true, "String");
        AttributeDescriptor atb6 = new AttributeDescriptor("att6", false, "String");
        ClassDescriptor cld3 = new ClassDescriptor("Class3", "Class2", false, new HashSet(Arrays.asList(new Object[] {atb5, atb6})),
                                                   new HashSet(), new HashSet());

        Set pks = new HashSet(Arrays.asList(new Object[] {atb5, atb3, atb1}));
        Model model = new Model("test", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));

        assertEquals(pks, cld3.getPkFieldDescriptors());
    }

    public void testGetSubDescriptors() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("Class1", null, false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("Class2", "Class1", false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("Class3", "Class1", false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld4 = new ClassDescriptor("Class4", "Class2", false, new HashSet(), new HashSet(), new HashSet());

        Model model2 = new Model("test2", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3, cld4})));

        // getSubDescriptrors should just return direct subclasses (cld2, cld3)
        Set subs = new HashSet(Arrays.asList(new Object[] {cld2, cld3}));

        assertEquals(subs, cld1.getSubDescriptors());
    }

    public void testGetImplementorDescriptors() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("Class2", "Interface1", false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("Class3", "Interface1", false, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld4 = new ClassDescriptor("Class4", "Class2", false, new HashSet(), new HashSet(), new HashSet());

        Model model2 = new Model("test2", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3, cld4})));

        // getImplementorDescriptrors should just return direct implementations (cld2, cld3)
        Set impls = new HashSet(Arrays.asList(new Object[] {cld2, cld3}));

        assertEquals(impls, cld1.getSubDescriptors());
    }

    public void testEquals() throws Exception {
        ClassDescriptor col1 = new ClassDescriptor("class1", null, true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col2 = new ClassDescriptor("class1", null, true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col3 = new ClassDescriptor("class1", "Super", true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col4 = new ClassDescriptor("class1", "Super", true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col5 = new ClassDescriptor("class1", "Interface", true, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col6 = new ClassDescriptor("class1", null, false, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor col7 = new ClassDescriptor("class1", null, true, Collections.singleton(new AttributeDescriptor("field", false, "int")), EMPTY_SET, EMPTY_SET);

        assertEquals(col1, col2);
        assertEquals(col1.hashCode(), col2.hashCode());
        assertFalse(col1.equals(col3));
        assertEquals(col3, col4);
        assertEquals(col3.hashCode(), col4.hashCode());
        assertFalse(col1.equals(col5));
        assertFalse(col1.equals(col6));
        assertFalse(col1.equals(col7));
    }

    public void testToString() throws Exception {
        ClassDescriptor cld1 = new ClassDescriptor("Interface1", null, true, new HashSet(), new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("Class2", null, false, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        ClassDescriptor cld3 = new ClassDescriptor("Class3", "Class2 Interface1", false, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        String expected = "<class name=\"Class3\" extends=\"Class2 Interface1\" is-interface=\"false\">"
            + "</class>";
        Model model = new Model("test", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
        assertEquals(expected, cld3.toString());
    }

    // ============================================

    private Set getAttributes() {
        Set attributes = new HashSet();
        AttributeDescriptor atd1 = new AttributeDescriptor("atd1", false, "String");
        AttributeDescriptor atd2 = new AttributeDescriptor("atd2", true, "Integer");
        attributes.add(atd1);
        attributes.add(atd2);
        return attributes;
    }

    private Set getReferences() {
        Set references = new HashSet();
        ReferenceDescriptor rfd1 = new ReferenceDescriptor("rfd1", false, "String", "reverse1");
        ReferenceDescriptor rfd2 = new ReferenceDescriptor("rfd2", true, "Integer", "reverse2");
        references.add(rfd1);
        references.add(rfd2);
        return references;
    }

    private Set getCollections() {
        Set collections = new HashSet();
        CollectionDescriptor cld1 = new CollectionDescriptor("cld1", false, "String", "reverse1", false);
        CollectionDescriptor cld2 = new CollectionDescriptor("cld2", true, "Integer", "reverse2", false);
        collections.add(cld1);
        collections.add(cld2);
        return collections;
    }

    public void testMultiInheritanceLegal() throws Exception {
        AttributeDescriptor atd1 = new AttributeDescriptor("atd1", false, "int");
        Set atds1 = new HashSet(Collections.singleton(atd1));
        AttributeDescriptor atd2 = new AttributeDescriptor("atd1", false, "int");
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, atds1, new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, atds2, new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        Model model = new Model("model", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
    }

    public void testMultiInheritanceIllegalAtt() throws Exception {
        AttributeDescriptor atd1 = new AttributeDescriptor("atd1", false, "int");
        Set atds1 = new HashSet(Collections.singleton(atd1));
        AttributeDescriptor atd2 = new AttributeDescriptor("atd1", false, "float");
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, atds1, new HashSet(), new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, atds2, new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultiInheritanceIllegalAttRef() throws Exception {
        ReferenceDescriptor atd1 = new ReferenceDescriptor("atd1", false, "package.name.Class2", null);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        AttributeDescriptor atd2 = new AttributeDescriptor("atd1", false, "float");
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), atds1, new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, atds2, new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultiInheritanceIllegalAttCol() throws Exception {
        CollectionDescriptor atd1 = new CollectionDescriptor("atd1", false, "package.name.Class2", null, false);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        AttributeDescriptor atd2 = new AttributeDescriptor("atd1", false, "float");
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), new HashSet(), atds1);
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, atds2, new HashSet(), new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultiInheritanceIllegalRefCol() throws Exception {
        CollectionDescriptor atd1 = new CollectionDescriptor("atd1", false, "package.name.Class2", null, false);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        ReferenceDescriptor atd2 = new ReferenceDescriptor("atd1", false, "package.name.Class2", null);
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), new HashSet(), atds1);
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, new HashSet(), atds2, new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultiInheritanceIllegalRef() throws Exception {
        ReferenceDescriptor atd1 = new ReferenceDescriptor("atd1", false, "package.name.Class2", null);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        ReferenceDescriptor atd2 = new ReferenceDescriptor("atd1", false, "package.name.Class2", null);
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), atds1, new HashSet());
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, new HashSet(), atds2, new HashSet());
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

    public void testMultiInheritanceIllegalCol() throws Exception {
        CollectionDescriptor atd1 = new CollectionDescriptor("atd1", false, "package.name.Class2", null, false);
        Set atds1 = new HashSet(Collections.singleton(atd1));
        CollectionDescriptor atd2 = new CollectionDescriptor("atd1", false, "package.name.Class2", null, false);
        Set atds2 = new HashSet(Collections.singleton(atd2));
        ClassDescriptor cld1 = new ClassDescriptor("package.name.Class1", null, true, new HashSet(), new HashSet(), atds1);
        ClassDescriptor cld2 = new ClassDescriptor("package.name.Class2", null, true, new HashSet(), new HashSet(), atds2);
        ClassDescriptor cld3 = new ClassDescriptor("package.name.Class3", "package.name.Class1 package.name.Class2", false, new HashSet(), new HashSet(), new HashSet());
        try {
            Model model = new Model("model", new HashSet(Arrays.asList(new Object[] {cld1, cld2, cld3})));
            fail("Expected: MetaDataException");
        } catch (MetaDataException e) {
        }
    }

}
