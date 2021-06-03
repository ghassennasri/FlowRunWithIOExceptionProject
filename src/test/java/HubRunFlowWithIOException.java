import com.marklogic.hub.flow.FlowInputs;
import com.marklogic.hub.flow.FlowRunner;
import com.marklogic.hub.flow.RunFlowResponse;
import com.marklogic.hub.flow.impl.FlowRunnerImpl;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.contrib.bmunit.WithByteman;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


@BMUnitConfig(debug = true,verbose = true)
@WithByteman
public class HubRunFlowWithIOException {
    @Test
    @BMRule(name="Throw IOException",
            isInterface = false,
            targetClass = "okhttp3.internal.connection.RealCall",
            targetMethod = "getResponseWithInterceptorChain$okhttp",
            //isOverriding = true,
            condition = "$this.originalRequest.toString().contains(\"/v1/resources/mlJobs\")",
            targetLocation ="AT ENTRY",
            action = "throw new java.io.IOException(\"Ghassen simulated IOException\")")
    public void runHubFlow(){
        // Create a FlowRunner instance.
        FlowRunner flowRunner = new FlowRunnerImpl(System.getProperty("host"), System.getProperty("username"), System.getProperty("password"));

        // Specify the flow to run.
        FlowInputs inputs = new FlowInputs(System.getProperty("flowName"));

        // To run only a subset of the steps in the flow, uncomment the following line and specify the sequence numbers of the steps to run.
        inputs.setSteps(new ArrayList<String>(Arrays.asList(System.getProperty("steps").split(","))));

        // Run the flow.
        RunFlowResponse response = flowRunner.runFlow(inputs);

        // Wait for the flow to end.
        flowRunner.awaitCompletion();


        // Display the response.
        System.out.println("Response: " + response);
        response.getStepResponses().forEach((x,y)->{
            System.out.println("--------------------------------------------");
            System.out.println("Step "+x);
            System.out.println("Response:"+y.stepOutput!=null?y.stepOutput.stream().collect(Collectors.joining()):"EMPTY");
            System.out.println("--------------------------------------------");
        });

    }
}
