/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.model.localvariable;

import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_OBJECT;
import static org.jd.core.v1.model.javasyntax.type.ObjectType.TYPE_UNDEFINED_OBJECT;

public class ObjectLocalVariable extends AbstractLocalVariable {
    protected TypeMaker typeMaker;
    protected Type type;

    public ObjectLocalVariable(TypeMaker typeMaker, int index, int offset, Type type, String name) {
        super(index, offset, name);
        this.typeMaker = typeMaker;
        this.type = type;
    }

    public ObjectLocalVariable(TypeMaker typeMaker, int index, int offset, Type type, String name, boolean declared) {
        this(typeMaker, index, offset, type, name);
        this.declared = declared;
    }

    public ObjectLocalVariable(TypeMaker typeMaker, int index, int offset, ObjectLocalVariable objectLocalVariable) {
        super(index, offset, null);
        this.typeMaker = typeMaker;
        this.type = objectLocalVariable.type;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if (!this.type.equals(type)) {
            this.type = type;
            fireChangeEvent();
        }
    }

    @Override
    public int getDimension() {
        return type.getDimension();
    }

    @Override
    public void accept(LocalVariableVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ObjectLocalVariable{");
        sb.append(type.getName());

        if (type.getDimension() > 0) {
            sb.append(new String(new char[type.getDimension()]).replaceAll("\0", "[]"));
        }

        sb.append(' ').append(name).append(", index=").append(index);

        if (next != null) {
            sb.append(", next=").append(next);
        }

        return sb.append("}").toString();
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        if (type.isObject()) {
            return typeMaker.isAssignable((ObjectType) this.type, (ObjectType) type);
        } else if (type.isGeneric()) {
            if (this.type.equals(TYPE_OBJECT)) {
                return true;
            }
        }

        return false;
    }

    public void typeOnRight(Type type) {
        if (type != TYPE_UNDEFINED_OBJECT) {
            if (this.type == TYPE_UNDEFINED_OBJECT) {
                this.type = type;
                fireChangeEvent();
            } else if ((this.type.getDimension() == 0) && (type.getDimension() == 0)) {
                assert !this.type.isPrimitive() && !type.isPrimitive() : "ObjectLocalVariable.typeOnRight(type) : unexpected type";

                if (this.type.isObject()) {
                    ObjectType thisObjectType = (ObjectType) this.type;

                    if (type.isObject()) {
                        ObjectType otherObjectType = (ObjectType) type;

                        if (thisObjectType.getInternalName().equals(otherObjectType.getInternalName())) {
                            if ((thisObjectType.getTypeArguments() == null) && (otherObjectType.getTypeArguments() != null)) {
                                // Keep type, update type arguments
                                this.type = otherObjectType;
                                fireChangeEvent();
                            }
                        } else if (typeMaker.isAssignable(thisObjectType, otherObjectType)) {
                            // Assignable types
                            if ((thisObjectType.getTypeArguments() == null) && (otherObjectType.getTypeArguments() != null)) {
                                // Keep type, update type arguments
                                this.type = thisObjectType.createType(otherObjectType.getTypeArguments());
                                fireChangeEvent();
                            }
                        }
                    }
                } else if (this.type.isGeneric()) {
                    if (type.isGeneric()) {
                        this.type = type;
                        fireChangeEvent();
                    }
                }
            }
        }
    }

    public void typeOnLeft(Type type) {
        if (type != TYPE_UNDEFINED_OBJECT) {
            if (this.type == TYPE_UNDEFINED_OBJECT) {
                this.type = type;
                fireChangeEvent();
            } else if ((this.type.getDimension() == 0) && (type.getDimension() == 0)) {
                assert !this.type.isPrimitive() && !type.isPrimitive() : "unexpected type in ObjectLocalVariable.typeOnLeft(type)";

                if (this.type.isObject()) {
                    ObjectType thisObjectType = (ObjectType) this.type;

                    if (type.isObject()) {
                        ObjectType otherObjectType = (ObjectType) type;

                        if (thisObjectType.getInternalName().equals(otherObjectType.getInternalName())) {
                            if ((thisObjectType.getTypeArguments() == null) && (otherObjectType.getTypeArguments() != null)) {
                                // Keep type, update type arguments
                                this.type = otherObjectType;
                                fireChangeEvent();
                            }
                        } else if (typeMaker.isAssignable(otherObjectType, thisObjectType)) {
                            // Assignable types
                            if ((thisObjectType.getTypeArguments() == null) && (otherObjectType.getTypeArguments() != null)) {
                                // Keep type, update type arguments
                                this.type = thisObjectType.createType(otherObjectType.getTypeArguments());
                                fireChangeEvent();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isAssignableFrom(AbstractLocalVariable variable) {
        return isAssignableFrom(variable.getType());
    }

    public void variableOnRight(AbstractLocalVariable variable) {
        addVariableOnRight(variable);
        typeOnRight(variable.getType());
    }

    public void variableOnLeft(AbstractLocalVariable variable) {
        addVariableOnLeft(variable);
        typeOnLeft(variable.getType());
    }
}
