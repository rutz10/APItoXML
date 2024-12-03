import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Step 1: Load the Excel mapping configuration
            List<ExcelMappingReader.XmlMapping> mappings = ExcelMappingReader.readMappings("field_mappings.xlsx");

            // Step 2: Prepare the API response data
            Campaign campaign1 = new Campaign("C001", "Winter Sale", "Ongoing");
            Campaign campaign2 = new Campaign("C002", "Black Friday Promo", "Upcoming");
            List<Campaign> campaigns = Arrays.asList(campaign1, campaign2);

            Task task1 = new Task("T001", "Develop API", "Completed");
            Task task2 = new Task("T002", "Code Review", "In Progress");
            Task task3 = new Task("T003", "Database Setup", "Not Started");
            List<Task> tasks1 = Arrays.asList(task1, task2);
            List<Task> tasks2 = Arrays.asList(task3);

            Member member1 = new Member("S101", "Michael Turner", "Lead Developer",
                    Arrays.asList("Java", "Spring Boot", "AWS"),
                    tasks1,
                    null); // No campaigns

            Member member2 = new Member("S102", "Emma Clark", "Backend Developer",
                    Arrays.asList("Node.js", "Express", "MongoDB"),
                    tasks2,
                    null); // No campaigns

            Member member3 = new Member("M101", "Lucas Scott", "Marketing Manager",
                    null, // No technologies
                    null, // No tasks
                    campaigns);

            Team team1 = new Team("Software Development", Arrays.asList(member1, member2));
            Team team2 = new Team("Marketing", Arrays.asList(member3));
            List<Team> teams1 = Arrays.asList(team1);
            List<Team> teams2 = Arrays.asList(team2);

            Branch branch1 = new Branch("North America", teams1);
            Branch branch2 = new Branch("Europe", teams2);
            List<Branch> branches = Arrays.asList(branch1, branch2);

            Company company = new Company("Global Enterprises", "London", branches);

            // Step 3: Convert the API response to XML
            String xmlOutput = XmlBuilder.buildXml(mappings, company);

            // Step 4: Print the XML
            System.out.println(xmlOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
