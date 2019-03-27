package usm.automation;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.LinkedHashSet;
import java.util.Set;

@XStreamAlias("ValCurs")
public final class Valutes {

    @XStreamAlias("Date")
    private String date;

    @XStreamAlias("Name")
    private String name;

    @XStreamImplicit
    private Set<Valute> valutes = new LinkedHashSet<>();

    public Valutes(String date, String name, Set<Valute> valutes) {
        setDate(date);
        setName(name);
        setValutes(valutes);
    }

    public Valutes(String date, String name) {
        setDate(date);
        setName(name);
    }

    private void setValutes(Set<Valute> valutes) {

        if (valutes == null)
            throw new IllegalArgumentException("Valute collection can not be null!");

        this.valutes = valutes;
    }

    private void setName(String name) {

        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Name can not be null/empty");

        this.name = name;
    }

    private void setDate(String date) {

        if (date == null || date.length() == 0)
            throw new IllegalArgumentException("Date can not be null/empty!");

        this.name = name;
    }

    public void addValute(Valute valute) {
        this.valutes.add(valute);
    }

    public Set<Valute> getValutes() {
        return new LinkedHashSet<>(valutes);
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }
}
