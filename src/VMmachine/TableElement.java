package VMmachine;

public class TableElement {
    public String name = "";
    public int dir;
    public int type;
    public int elementInt;
    public int vs;

    TableElement(){
    }

    TableElement(TableElement TE){
        this.name = TE.name;
        this.dir = TE.dir;
        this.type = TE.type;
        this.elementInt = TE.elementInt;
        this.vs = TE.vs;
    }

    TableElement(String name, int dir, int type, int elementNInt, int vs){
        this.name = name;
        this.dir = dir;
        this.type = type;
        this.elementInt = elementNInt;
        this.vs = vs;
    }
/*
*  public void updateValue(String newValue) {
        this.value = newValue;
    }

    public void incValue() {
        this.value = Integer.toString(Integer.parseInt(this.value)+1);
    }

    public void decValue() {
        this.value = Integer.toString(Integer.parseInt(this.value)-1);
    }*/

/*
    TableElement (String name, int dir, int type, int elementDouble, int vs, double value){
        this.name = name;
        this.dir = dir;
        this.type = type;
        this.elementDouble = elementDouble;
        this.vs = vs;
    }

    TableElement (String name, int dir, int type,  elementString, int vs){
        this.name = name;
        this.dir = dir;
        this.type = type;
        this.elementString = elementString;
        this.vs = vs;
    }
*/

}
