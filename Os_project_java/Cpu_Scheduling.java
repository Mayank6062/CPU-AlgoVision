//rr - 4
//srt 
//agin(.) 
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class Cpu_Scheduling {
    // Global variables
    static String operation; // Variable to hold the operation mode (trace or stats)
    static int lastInstant, processCount; // Last time instant and total number of processes
    static List<Map.Entry<Character, Integer>> algorithms = new ArrayList<>(); // List to hold scheduling algorithms with their parameters
    static List<Process> processes = new ArrayList<>(); // List to store process information
    static List<List<Character>> timeline = new ArrayList<>(); // Timeline for process execution visualization
    static Map<String, Integer> processToIndex = new HashMap<>(); // Mapping of process names to their indices

    // Results lists for storing calculated times
    static List<Integer> finishTime = new ArrayList<>(); // List to hold finish times of processes
    static List<Integer> turnAroundTime = new ArrayList<>(); // List to hold turnaround times of processes
    static List<Float> normTurn = new ArrayList<>(); // List to hold normalized turnaround times of processes

    // Class for storing process information
    static class Process {
        String name; // Process name
        int arrivalTime; // Arrival time of the process
        int serviceTime; // Service time of the process

        // Constructor to initialize process attributes
        Process(String name, int arrivalTime, int serviceTime) {
            this.name = name;
            this.arrivalTime = arrivalTime;
            this.serviceTime = serviceTime;
        }
    }

    // Method to parse algorithms from a given string
    public static void parseAlgorithms(String algorithmChunk) {
        // Split the input string by commas to get individual algorithms
        String[] algorithmsArray = algorithmChunk.split(",");
        for (String tempStr : algorithmsArray) {
            // Split each algorithm string by '-' to separate ID and quantum time
            String[] parts = tempStr.split("-");
            char algorithmId = parts[0].charAt(0); // Get algorithm ID
            // Parse quantum time if available; otherwise set it to -1
            int quantum = parts.length >= 2 ? Integer.parseInt(parts[1]) : -1;
            // Add the algorithm as a key-value pair to the algorithms list
            algorithms.add(new AbstractMap.SimpleEntry<>(algorithmId, quantum));
        }
    }

    // Method to parse process information from the scanner input
    public static void parseProcesses(Scanner sc) {
        // Loop through the number of processes to read their details
        for (int i = 0; i < processCount; i++) {
            // Read the next process chunk
            String processChunk = sc.next();
            // Split the chunk to extract process name, arrival time, and service time
            String[] processParts = processChunk.split(",");
            String processName = processParts[0]; // Get process name
            int processArrivalTime = Integer.parseInt(processParts[1]); // Get arrival time
            int processServiceTime = Integer.parseInt(processParts[2]); // Get service time

            // Create a new Process object and add it to the processes list
            processes.add(new Process(processName, processArrivalTime, processServiceTime));
            // Map the process name to its index for quick lookup
            processToIndex.put(processName, i);
        }
    }

    // Main parsing method to read all input data
    public static void parse(Scanner sc) {
        // Read operation mode (trace or stats)
        operation = sc.next();
        // Read algorithms chunk and the last time instant
        String algorithmChunk = sc.next();
        lastInstant = sc.nextInt(); // Get last time instant
        processCount = sc.nextInt(); // Get the number of processes

        // Call methods to parse algorithms and processes
        parseAlgorithms(algorithmChunk);
        parseProcesses(sc);

        // Initialize finish time, turnaround time, and normalized turnaround time lists with zeros
        finishTime = new ArrayList<>(Collections.nCopies(processCount, 0));
        turnAroundTime = new ArrayList<>(Collections.nCopies(processCount, 0));
        normTurn = new ArrayList<>(Collections.nCopies(processCount, 0.0f));

        // Initialize the timeline for process execution visualization
        timeline = new ArrayList<>();
        for (int i = 0; i < lastInstant; i++) {
            // Create a new list for each time instant filled with spaces
            timeline.add(new ArrayList<>(Collections.nCopies(processCount, ' ')));
        }
    }

    /** Global Constants **/
    static final String TRACE = "trace"; // Constant for trace operation mode
    static final String SHOW_STATISTICS = "stats"; // Constant for showing statistics operation mode
    static final String[] ALGORITHMS = {"", "FCFS", "RR-", "SPN", "SRT", "HRRN", "FB-1", "FB-2i", "AGING"}; // Algorithm names

    // Comparator to sort processes by service time
    public static Comparator<Process> sortByServiceTime = Comparator.comparingInt(a -> a.serviceTime);

    // Comparator to sort processes by arrival time
    public static Comparator<Process> sortByArrivalTime = Comparator.comparingInt(a -> a.arrivalTime);

    // Comparator to sort entries by response ratio in descending order
    public static Comparator<Map.Entry<String, Double>> descendinglyByResponseRatio = (a, b) -> b.getValue().compareTo(a.getValue());

    // Comparator to sort priority levels of processes
    public static Comparator<SimpleEntry<Integer, Integer>> byPriorityLevel = (a, b) -> {
        // If priority levels are equal, sort by secondary criteria
        if (a.getKey().equals(b.getKey())) {
            return b.getValue().compareTo(a.getValue());
        }
        // Otherwise, sort by priority level
        return b.getKey().compareTo(a.getKey());
    };

    // Calculate response ratio based on wait time and service time
    public static double calculateResponseRatio(int waitTime, int serviceTime) {
        return (waitTime + serviceTime) * 1.0 / serviceTime; // Calculate and return the response ratio
    }

    // Clear the timeline for a fresh execution
    public static void clearTimeline() {
        for (int i = 0; i < lastInstant; i++) {
            for (int j = 0; j < processCount; j++) {
                // Set all timeline entries to a space character
                timeline.get(i).set(j, ' ');
            }
        }
    }

    // Getters for process fields (equivalent to getProcessName, getArrivalTime, getServiceTime)
    public static String getProcessName(Process p) {
        return p.name; // Return the name of the process
    }

    public static int getArrivalTime(Process p) {
        return p.arrivalTime; // Return the arrival time of the process
    }

    public static int getServiceTime(Process p) {
        return p.serviceTime; // Return the service time of the process
    }

    public static int getPriorityLevel(Process p) {
        return p.serviceTime; // Return the service time as priority level
    }

    // Fill in wait time in the timeline based on finish times
    public static void fillInWaitTime() {
        // Loop through all processes
        for (int i = 0; i < processCount; i++) {
            int arrivalTime = getArrivalTime(processes.get(i)); // Get arrival time
            // For each time from arrival to finish time, mark wait time in the timeline
            for (int k = arrivalTime; k < finishTime.get(i); k++) {
                // If the timeline entry is not already filled, set it to indicate wait time
                if (timeline.get(k).get(i) != '*') {
                    timeline.get(k).set(i, '.'); // Mark wait time with '.'
                }
            }
        }
    }

    


//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 
public static void firstComeFirstServe() {
    int time = 0; // Initialize the current time to 0, representing the start of the scheduling

    // Iterate through each process based on their arrival order
    for (int i = 0; i < processCount; i++) { 
        // Retrieve the arrival time for the current process using a helper function
        int arrivalTime = getArrivalTime(processes.get(i)); 
        
        // Retrieve the service time for the current process using a helper function
        int serviceTime = getServiceTime(processes.get(i)); 

        // Ensure the current time is updated to the process's arrival time if it arrives later
        time = Math.max(time, arrivalTime); // Set 'time' to the maximum of the current time and the arrival time of the process

        // Calculate and store the finish time for the current process
        finishTime.set(i, time + serviceTime); // Finish time is calculated as the current time plus the service time
        
        // Calculate the turnaround time for the current process (Total time taken from arrival to completion)
        turnAroundTime.set(i, finishTime.get(i) - arrivalTime); // Turnaround time is the difference between finish time and arrival time
        
        // Calculate the normalized turnaround time (Turnaround time divided by service time)
        normTurn.set(i, (float) turnAroundTime.get(i) / serviceTime); // Store normalized turnaround time for the current process

        // Update the timeline to visualize the scheduling
        for (int j = time; j < finishTime.get(i); j++) { // Loop through each time unit from current time to finish time
            // Ensure the timeline has enough entries to accommodate the current time
            while (timeline.size() <= j) { 
                // Create a new entry filled with spaces for all processes in the timeline
                timeline.add(new ArrayList<>(Collections.nCopies(processCount, ' '))); 
            }
            // Mark the service time for the current process with an asterisk (*) in the timeline
            timeline.get(j).set(i, '*'); // Indicates that the process is being serviced during this time
        }

        // Mark the waiting time in the timeline for the time units before the process starts
        for (int j = arrivalTime; j < time; j++) { // Loop through each time unit from arrival time to current time
            // Ensure the timeline has enough entries to accommodate the current time
            while (timeline.size() <= j) { 
                // Create a new entry filled with spaces for all processes in the timeline
                timeline.add(new ArrayList<>(Collections.nCopies(processCount, ' '))); 
            }
            // Mark the waiting time for the current process with a dot (.) in the timeline
            timeline.get(j).set(i, '.'); // Indicates that the process is waiting during this time
        }

        // Advance the current time by the service time of the current process
        time += serviceTime; // Update the current time to reflect the completion of the current process
    }
    // Call a method to fill in waiting times for all processes after scheduling is complete
    fillInWaitTime(); // Finalize the waiting times for each process in the timeline
}





//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------





// Pair class for holding two related values
static class Pair<K, V> {
    private K key; // Key of the pair, typically the process index
    private V value; // Value of the pair, typically the remaining service time

    // Constructor to initialize a Pair with key and value
    public Pair(K key, V value) {
        this.key = key; // Assign the provided key to the key field
        this.value = value; // Assign the provided value to the value field
    }

    // Getter method for the key
    public K getKey() {
        return key; // Return the key
    }

    // Getter method for the value
    public V getValue() {
        return value; // Return the value
    }

    // Setter method for the value
    public void setValue(V value) {
        this.value = value; // Update the value with the new provided value
    }
}

public static void roundRobin(int originalQuantum) {
    // Queue to manage processes with remaining service times
    Queue<Pair<Integer, Integer>> q = new LinkedList<>(); 
    int j = 0; // Index to track the next process to arrive

    // Initialize the queue with the first process if it arrives at time 0
    if (getArrivalTime(processes.get(j)) == 0) { 
        // Add the first process to the queue with its service time
        q.add(new Pair<>(j, getServiceTime(processes.get(j))));
        j++; // Move to the next process
    }

    int currentQuantum = originalQuantum; // Variable to track the current quantum time

    // Main loop to simulate time and process scheduling
    for (int time = 0; time < lastInstant; time++) { // Loop until the last time unit
        if (!q.isEmpty()) { // Check if there are processes in the queue
            // Peek at the front of the queue to get the current process index
            int processIndex = q.peek().getKey(); 
            // Get the remaining service time for the current process and decrement it by 1 (time unit)
            int remainingServiceTime = q.peek().getValue() - 1; 
            int arrivalTime = getArrivalTime(processes.get(processIndex)); // Get arrival time for the current process
            int serviceTime = getServiceTime(processes.get(processIndex)); // Get service time for the current process
            currentQuantum--; // Decrease the remaining quantum time
            timeline.get(time).set(processIndex, '*'); // Mark the current process as running in the timeline

            // Check for new arrivals at this time (time + 1)
            while (j < processCount && getArrivalTime(processes.get(j)) == time + 1) { 
                // Add new arriving processes to the queue with their service times
                q.add(new Pair<>(j, getServiceTime(processes.get(j))));
                j++; // Move to the next process
            }

            // Case 1: If current quantum expires and process has finished execution
            if (currentQuantum == 0 && remainingServiceTime == 0) {
                // Set finish time for the current process
                finishTime.set(processIndex, time + 1);
                // Calculate turnaround time for the current process
                turnAroundTime.set(processIndex, finishTime.get(processIndex) - arrivalTime);
                // Calculate normalized turnaround time
                normTurn.set(processIndex, turnAroundTime.get(processIndex) * 1.0f / serviceTime);
                currentQuantum = originalQuantum; // Reset the quantum time for the next process
                q.poll(); // Remove the completed process from the queue

            // Case 2: If current quantum expires but process still has remaining time
            } else if (currentQuantum == 0 && remainingServiceTime != 0) {
                q.poll(); // Remove the current process from the queue
                // Add the current process back with its updated remaining service time
                q.add(new Pair<>(processIndex, remainingServiceTime)); 
                currentQuantum = originalQuantum; // Reset the quantum time for the next process

            // Case 3: If current quantum has not expired but the process has finished execution
            } else if (currentQuantum != 0 && remainingServiceTime == 0) {
                // Set finish time for the current process
                finishTime.set(processIndex, time + 1);
                // Calculate turnaround time for the current process
                turnAroundTime.set(processIndex, finishTime.get(processIndex) - arrivalTime);
                // Calculate normalized turnaround time
                normTurn.set(processIndex, turnAroundTime.get(processIndex) * 1.0f / serviceTime);
                q.poll(); // Remove the completed process from the queue
                currentQuantum = originalQuantum; // Reset the quantum time for the next process

            // Case 4: If neither the quantum nor the service time has expired
            } else {
                // Update the remaining service time in the queue
                q.poll(); // Remove the current process from the queue
                // Re-add the current process with the updated remaining service time
                q.add(new Pair<>(processIndex, remainingServiceTime)); 
            }
        }

        // Check for new arrivals at this time
        while (j < processCount && getArrivalTime(processes.get(j)) == time + 1) { 
            // Add any new arriving processes to the queue with their service times
            q.add(new Pair<>(j, getServiceTime(processes.get(j))));
            j++; // Move to the next process
        }
    }
    
    // Finalize waiting times for all processes after the scheduling simulation
    fillInWaitTime(); // Fill wait times for all processes after scheduling is complete
}


//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------




public static void shortestProcessNext() {
    // Create a priority queue to manage processes based on their service time
    PriorityQueue<Pair<Integer, Integer>> pq = new PriorityQueue<>(Comparator.comparingInt(Pair::getKey));
    int j = 0; // Index to track the next process to arrive

    // Iterate through each time unit until the last instant
    for (int i = 0; i < lastInstant; i++) { 
        // Check for new process arrivals at the current time unit
        while (j < processCount && getArrivalTime(processes.get(j)) <= i) {
            // Add the newly arrived process to the priority queue based on its service time
            pq.add(new Pair<>(getServiceTime(processes.get(j)), j)); 
            j++; // Move to the next process
        }

        // If there are processes in the queue, execute the one with the shortest service time
        if (!pq.isEmpty()) {
            // Get the process index of the shortest job
            int processIndex = pq.peek().getValue(); 
            int arrivalTime = getArrivalTime(processes.get(processIndex)); // Get its arrival time
            int serviceTime = getServiceTime(processes.get(processIndex)); // Get its service time
            pq.poll(); // Remove the process from the queue as we are about to execute it

            // Mark the timeline with '.' for the time units from arrival time to current time
            for (int temp = arrivalTime; temp < i; temp++) { 
                timeline.get(temp).set(processIndex, '.'); // Indicate waiting period
            }

            // Mark the timeline with '*' for the time units during which the process is executing
            for (int temp = i; temp < i + serviceTime; temp++) {
                timeline.get(temp).set(processIndex, '*'); // Indicate execution period
            }

            // Set the finish time for the current process
            finishTime.set(processIndex, i + serviceTime);
            // Calculate turnaround time for the current process
            turnAroundTime.set(processIndex, finishTime.get(processIndex) - arrivalTime);
            // Calculate normalized turnaround time
            normTurn.set(processIndex, turnAroundTime.get(processIndex) * 1.0f / serviceTime);

            // Advance the current time to the end of the executed process
            i += serviceTime - 1; // -1 because 'i' will be incremented in the for loop
        }
    }
    // Finalize waiting times for all processes after the scheduling simulation
    fillInWaitTime(); // Fill wait times for all processes after scheduling is complete
}




//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------




public static void shortestRemainingTime() {
    // Priority queue for pairs of remaining service time and process index (min-heap)
    PriorityQueue<Pair<Integer, Integer>> pq = new PriorityQueue<>(Comparator.comparingInt(Pair::getKey));
    int j = 0; // Index to track the next process to arrive

    // Iterate through each time unit until the last instant
    for (int i = 0; i < lastInstant; i++) { 
        // Add processes that have arrived at the current time 'i'
        while (j < processCount && getArrivalTime(processes.get(j)) == i) {
            // Add the newly arrived process to the priority queue with its service time
            pq.add(new Pair<>(getServiceTime(processes.get(j)), j));
            j++; // Move to the next process
        }

        // Process the one with the shortest remaining time
        if (!pq.isEmpty()) {
            // Peek at the process with the shortest remaining time
            int processIndex = pq.peek().getValue(); // Get the index of the process
            int remainingTime = pq.peek().getKey(); // Get its remaining service time
            pq.poll(); // Remove the process from the queue as we are about to execute it

            int serviceTime = getServiceTime(processes.get(processIndex)); // Get total service time
            int arrivalTime = getArrivalTime(processes.get(processIndex)); // Get arrival time

            // Mark the timeline for the current process
            timeline.get(i).set(processIndex, '*'); 

            // Check if the process is finished
            if (remainingTime == 1) { // Process finished
                // Set the finish time for the current process
                finishTime.set(processIndex, i + 1);
                // Calculate turnaround time (completion - arrival)
                turnAroundTime.set(processIndex, finishTime.get(processIndex) - arrivalTime);
                // Calculate normalized turnaround time (turnaround / service time)
                normTurn.set(processIndex, turnAroundTime.get(processIndex) * 1.0f / serviceTime);
            } else {
                // Decrement remaining time and re-add to the queue
                pq.add(new Pair<>(remainingTime - 1, processIndex)); 
            }
        }
    }

    fillInWaitTime(); // Fill in wait times after processing
}



//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------



static class ProcessInfo {
    String processName;
    double responseRatio;
    int timeInService;

    ProcessInfo(String processName, double responseRatio, int timeInService) {
        this.processName = processName;
        this.responseRatio = responseRatio;
        this.timeInService = timeInService;
    }
}

public static void highestResponseRatioNext() {
    List<ProcessInfo> presentProcesses = new ArrayList<>(); // List of ProcessInfo holding process name, response ratio, and service time
    int j = 0; // Index to track the next process to arrive

    for (int currentInstant = 0; currentInstant < lastInstant; currentInstant++) { 
        // Add processes that have arrived at the current time 'currentInstant'
        while (j < processCount && getArrivalTime(processes.get(j)) <= currentInstant) {
            presentProcesses.add(new ProcessInfo(getProcessName(processes.get(j)), 1.0, 0)); // Initial response ratio is set to 1.0
            j++; // Move to the next process
        }

        // Calculate response ratio for every process
        for (ProcessInfo proc : presentProcesses) {
            String processName = proc.processName;
            int processIndex = processToIndex.get(processName); // Get index of process by name
            int waitTime = currentInstant - getArrivalTime(processes.get(processIndex)); // Calculate wait time
            int serviceTime = getServiceTime(processes.get(processIndex)); // Get total service time
            proc.responseRatio = calculateResponseRatio(waitTime, serviceTime); // Calculate response ratio
        }

        // Sort present processes by highest to lowest response ratio
        presentProcesses.sort((a, b) -> {
            // First compare by response ratio (descending)
            int cmp = Double.compare(b.responseRatio, a.responseRatio);
            // If equal, sort by process name (ascending) for stable sorting
            return cmp != 0 ? cmp : a.processName.compareTo(b.processName);
        });

        if (!presentProcesses.isEmpty()) {
            // Get the process with the highest response ratio
            int processIndex = processToIndex.get(presentProcesses.get(0).processName);
            while (currentInstant < lastInstant && presentProcesses.get(0).timeInService != getServiceTime(processes.get(processIndex))) {
                timeline.get(currentInstant).set(processIndex, '*'); // Mark the timeline for the current process
                currentInstant++; // Move to the next time unit
                presentProcesses.get(0).timeInService++; // Increment the time in service for the current process
            }
            currentInstant--; // Adjust currentInstant after the while loop
            presentProcesses.remove(0); // Remove the completed process
            // Update finish time, turnaround time, and normalized turnaround time for the completed process
            finishTime.set(processIndex, currentInstant + 1);
            turnAroundTime.set(processIndex, finishTime.get(processIndex) - getArrivalTime(processes.get(processIndex)));
            normTurn.set(processIndex, (turnAroundTime.get(processIndex) * 1.0f / getServiceTime(processes.get(processIndex))));
        }
    }
    fillInWaitTime(); // Fill in wait times after processing
}



//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------



static class Tuple {
    int priorityLevel; // Current priority level of the process
    int processIndex;  // Index of the process in the original list
    int waitingTime;   // Total waiting time of the process

    Tuple(int priorityLevel, int processIndex, int waitingTime) {
        this.priorityLevel = priorityLevel;
        this.processIndex = processIndex;
        this.waitingTime = waitingTime;
    }
}

public static void aging(int originalQuantum) {
    List<Tuple> v = new ArrayList<>(); // List of tuples holding priority level, process index, and total waiting time
    int j = 0; // Index for processes
    int currentProcess = -1; // Current executing process index

    for (int time = 0; time < lastInstant; time++) { // Iterate through each time unit
        // Add processes that have arrived at the current time 'time'
        while (j < processCount && processes.get(j).arrivalTime <= time) {
            v.add(new Tuple(getPriorityLevel(processes.get(j)), j, 0)); // Create a tuple for the process
            j++; // Move to the next process
        }

        // Update waiting times and priority levels
        for (int i = 0; i < v.size(); i++) {
            if (v.get(i).processIndex == currentProcess) {
                // If it's the currently executing process
                v.get(i).waitingTime = 0; // Reset waiting time
                v.get(i).priorityLevel = getPriorityLevel(processes.get(currentProcess)); // Reset priority
            } else {
                // If it's a waiting process
                v.get(i).priorityLevel++; // Increase priority level due to waiting
                v.get(i).waitingTime++; // Increment waiting time
            }
        }

        // If there are processes to schedule
        if (!v.isEmpty()) {
            // Sort processes by priority level (descending) and then by process index (ascending)
            v.sort(Comparator.comparingInt((Tuple t) -> t.priorityLevel).reversed()
                              .thenComparingInt(t -> t.processIndex));

            // Select the process with the highest priority
            currentProcess = v.get(0).processIndex;
            int currentQuantum = originalQuantum; // Quantum time for the selected process
            while (currentQuantum-- > 0 && time < lastInstant) {
                timeline.get(time).set(currentProcess, '*'); // Mark the timeline for the current process
                time++; // Move to the next time unit
            }
            time--; // Adjust time after the loop
        }
    }
    fillInWaitTime(); // Fill in wait times after processing
}


//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------




public static void executeAlgorithm(char algorithmId, int quantum, String operation) {
    switch (algorithmId) {
        case '1':
            if (operation.equals(TRACE)) System.out.print("FCFS  ");
            firstComeFirstServe();
            break;
        case '2':
            if (operation.equals(TRACE)) System.out.print("RR-" + quantum + "  ");
            roundRobin(quantum);
            break;
        case '3':
            if (operation.equals(TRACE)) System.out.print("SPN   ");
            shortestProcessNext();
            break;
        case '4':
            if (operation.equals(TRACE)) System.out.print("SRT   ");
            shortestRemainingTime();
            break;
        case '5':
            if (operation.equals(TRACE)) System.out.print("HRRN  ");
            highestResponseRatioNext();
            break;
        case '6':
            if (operation.equals(TRACE)) System.out.print("Aging ");
            aging(quantum);
            break;
        default:
            break;
    }
}

//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


// Print functions

// This function prints the name of the scheduling algorithm being used.
// It retrieves the algorithm ID and checks if it corresponds to a specific algorithm (ID 2) 
// to include any additional value associated with it.
public static void printAlgorithm(int algorithmIndex) {
    int algorithmId = algorithms.get(algorithmIndex).getKey() - '0'; // Convert character to int
    // If algorithm ID is 2, print the algorithm name with its associated value
    if (algorithmId == 2) {
        System.out.println(ALGORITHMS[algorithmId] + algorithms.get(algorithmIndex).getValue());
    } else {
        System.out.println(ALGORITHMS[algorithmId]); // Otherwise, just print the algorithm name
    }
}



// This function prints a formatted list of all processes' names.
// It starts with a header "Process" and displays each process name below it.
public static void printProcesses() {
    System.out.print("Process    ");
    for (int i = 0; i < processCount; i++)
        System.out.print("|  " + getProcessName(processes.get(i)) + "  "); // Print each process name
    System.out.println("|"); // Close the row
}



// This function prints the arrival times of all processes in a structured manner.
// It starts with a header "Arrival" and aligns each arrival time accordingly.
public static void printArrivalTime() {
    System.out.print("Arrival    ");
    for (int i = 0; i < processCount; i++)
        System.out.printf("|%3d  ", getArrivalTime(processes.get(i))); // Print arrival time with formatting
    System.out.println("|"); // Close the row
}



// This function prints the service (or burst) times of all processes.
// It includes a "Mean" label at the end for future calculations of average service time.
public static void printServiceTime() {
    System.out.print("Service    |");
    for (int i = 0; i < processCount; i++)
        System.out.printf("%3d  |", getServiceTime(processes.get(i))); // Print each service time
    System.out.println(" Mean|"); // Close the row and indicate mean label
}



// This function prints the finish times of all processes after they have completed execution.
// Each finish time is printed under the header "Finish" for clarity.
public static void printFinishTime() {
    System.out.print("Finish     ");
    for (int i = 0; i < processCount; i++)
        System.out.printf("|%3d  ", finishTime.get(i)); // Print each finish time
    System.out.println("|-----|"); // Close the row with dashes for visual separation
}



// This function calculates and prints the turnaround times of the processes,
// which is the total time taken for a process to complete since its arrival.
// It also computes and prints the average turnaround time.
public static void printTurnAroundTime() {
    System.out.print("Turnaround |");
    int sum = 0; // Initialize sum to calculate average
    for (int i = 0; i < processCount; i++) {
        System.out.printf("%3d  |", turnAroundTime.get(i)); // Print each turnaround time
        sum += turnAroundTime.get(i); // Add to the sum for average calculation
    }
    System.out.printf(" %2.2f|\n", (1.0 * sum / turnAroundTime.size())); // Print average turnaround time
}



// This function prints the normalized turnaround times of all processes.
// It also computes and prints the average normalized turnaround time.
public static void printNormTurn() {
    System.out.print("NormTurn   |");
    float sum = 0; // Initialize sum for average calculation
    for (int i = 0; i < processCount; i++) {
        System.out.printf(" %2.2f|", normTurn.get(i)); // Print each normalized turnaround time
        sum += normTurn.get(i); // Add to the sum for average calculation
    }
    System.out.printf(" %2.2f|\n", (1.0 * sum / normTurn.size())); // Print average normalized turnaround time
}



// This function consolidates and calls all individual print functions to display
// the algorithm and process statistics in a structured format.
public static void printStats(int algorithmIndex) {
    printAlgorithm(algorithmIndex); // Print the algorithm being used
    printProcesses(); // Print the list of processes
    printArrivalTime(); // Print the arrival times of processes
    printServiceTime(); // Print the service times of processes
    printFinishTime(); // Print the finish times of processes
    printTurnAroundTime(); // Print turnaround times
    printNormTurn(); // Print normalized turnaround times
}



//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

// This function prints a timeline of process execution over time.
// It visually represents the time slots occupied by each process in the timeline.
public static void printTimeline(int algorithmIndex) {
    // Print the time slots (0 to lastInstant) at the top of the timeline
    for (int i = 0; i <= lastInstant; i++)
        System.out.print(i % 10 + " "); // Print time units modulo 10 for readability
    System.out.println();
    System.out.println("------------------------------------------------"); // Separator line

    // Print each process name followed by its execution timeline
    for (int i = 0; i < processCount; i++) {
        System.out.print(processes.get(i).name + "     |"); // Print process name
        for (int j = 0; j < lastInstant; j++) {
            System.out.print(timeline.get(j).get(i) + "|"); // Print the execution timeline for each process
        }
        System.out.println(" "); // New line for the next process
    }
    System.out.println("------------------------------------------------"); // End separator line
}


//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


public static void main(String[] args) {
    // Input handling: Create a Scanner object to read user input from the console.
    Scanner scanner = new Scanner(System.in);

    // Parse the initial data: Call the parse method, which is responsible for reading and interpreting input data.
    parse(scanner); // This could include reading process details, algorithm types, etc.
    
    // Loop through all the algorithms available in the algorithms list.
    for (int idx = 0; idx < algorithms.size(); idx++) {
        // Execute the scheduling algorithm:
        // Call executeAlgorithm with the algorithm key, value, and operation mode.
        // The key might represent the algorithm type (e.g., FCFS, SJF), and the value might represent additional parameters.
        executeAlgorithm(algorithms.get(idx).getKey(), algorithms.get(idx).getValue(), operation);

        // Determine the operation mode: Check the value of the 'operation' variable
        // and decide whether to print the timeline of process execution or display statistics.
        if (operation.equals(TRACE)) {
            // If the operation is TRACE, call printTimeline to visualize the execution of processes over time.
            printTimeline(idx); // idx is used to reference the specific algorithm being executed.
        } else if (operation.equals(SHOW_STATISTICS)) {
            // If the operation is SHOW_STATISTICS, call printStats to display relevant metrics for the executed algorithm.
            printStats(idx); // idx again references the current algorithm for which statistics are needed.
        }

        // Print a new line after processing each algorithm for better readability of output.
        System.out.println(); // This helps to separate outputs for different algorithms visually.
    }

    // Close the scanner to free up resources and avoid memory leaks.
    scanner.close();
}
}