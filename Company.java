import java.util.List;

public class Company {
    private String name;
    private String location;
    private List<Branch> branches;

    // Constructors
    public Company() {}

    public Company(String name, String location, List<Branch> branches) {
        this.name = name;
        this.location = location;
        this.branches = branches;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public List<Branch> getBranches() {
        return branches;
    }
}
