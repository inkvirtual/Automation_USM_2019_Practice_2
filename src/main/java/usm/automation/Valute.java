package usm.automation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("Valute")
public final class Valute {

    @XStreamAsAttribute
    @XStreamAlias("ID")
    private long id;

    @XStreamAlias("NumCode")
    private String numCode;

    @XStreamAlias("CharCode")
    private String charCode;

    @XStreamAlias("Nominal")
    private int nominal;

    @XStreamAlias("Name")
    private String name;

    @XStreamAlias("Value")
    private double value;

    public Valute(long id, String numCode, String charCode, int nominal, String name, double value) {
        this.id = id;
        this.numCode = numCode;
        this.charCode = charCode;
        this.nominal = nominal;
        this.name = name;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public String getNumCode() {
        return numCode;
    }

    public String getCharCode() {
        return charCode;
    }

    public int getNominal() {
        return nominal;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Valute{" +
                "id=" + id +
                ", numCode=" + numCode +
                ", charCode='" + charCode + '\'' +
                ", nominal=" + nominal +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
