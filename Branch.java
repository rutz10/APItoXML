import java.util.List;

public class Branch {
    private String branchName;
    private List<Team> teams;

    // Constructors
    public Branch() {}

    public Branch(String branchName, List<Team> teams) {
        this.branchName = branchName;
        this.teams = teams;
    }

    // Getters and Setters
    public String getBranchName() {
        return branchName;
    }

    public List<Team> getTeams() {
        return teams;
    }
}
