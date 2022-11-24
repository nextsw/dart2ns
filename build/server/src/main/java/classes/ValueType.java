package classes;

import d3e.core.IterableExt;
import d3e.core.ListExt;
import d3e.core.StringExt;
import java.util.List;
import java.util.Set;

public class ValueType extends DataType {
  public String in;
  public List<DataType> args = ListExt.asList();

  public ValueType(String name, boolean optional) {
    this.name = name;
    this.optional = optional;
  }

  public String toString() {
    String res = this.name;
    if (ListExt.isNotEmpty(this.args)) {
      res = res + "<" + this.args.toString() + ">";
    }
    return res;
  }

  public void collectUsedTypes(Set<String> types) {
    types.add(this.name);
  }

  public PropType type(ValidationContext ctx) {
    PropType propType;
    if (StringExt.length(this.name) == 1l) {
      propType = ctx.getType(this.name);
    } else {
      propType = ctx.getType(this.name);
    }
    if (propType == null) {
      ctx.addError(null, "Unknown type :" + this.name);
      propType = ctx.object();
    } else {
      ctx.addUsedType(propType);
    }
    propType = applyArgsData(ctx, propType);
    return propType;
  }

  public PropType rawType(ValidationContext ctx) {
    PropType realType = ctx.getRawType(this.name);
    realType = applyRawArgsData(ctx, realType);
    return realType;
  }

  public PropType applyArgsData(ValidationContext ctx, PropType type) {
    return type.applyArgs(
        IterableExt.toList(
            ListExt.map(
                this.args,
                (a) -> {
                  return a.type(ctx);
                }),
            false));
  }

  public PropType applyRawArgsData(ValidationContext ctx, PropType type) {
    return type.applyArgs(
        IterableExt.toList(
            ListExt.map(
                this.args,
                (a) -> {
                  return rawType(ctx);
                }),
            false));
  }

  public List<PropType> createTypesArray(ValidationContext ctx) {
    return IterableExt.toList(
        ListExt.map(
            this.args,
            (a) -> {
              return a.type(ctx);
            }),
        false);
  }

  public List<DataType> getTypeArguments() {
    return this.args;
  }
}
