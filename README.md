## GraalVM 22.2

- [User Guide](https://medium.com/graalvm/graalvm-22-2-smaller-jdk-size-improved-memory-usage-better-library-support-and-more-cb34b5b68ec0)
- [API Reference](https://www.graalvm.org/22.2/docs/getting-started/#run-java)
---

## Micronaut 3.7.2 Documentation

- [User Guide](https://docs.micronaut.io/3.7.2/guide/index.html)
- [API Reference](https://docs.micronaut.io/3.7.2/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/3.7.2/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

## Handler

[AWS Lambda Handler](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html)

Handler: io.micronaut.function.aws.proxy.MicronautLambdaHandler


## Deployment with GraalVM

If you want to deploy to AWS Lambda as a GraalVM native image, run:

```bash
./mvnw package -Dpackaging=docker-native -Dmicronaut.runtime=lambda -Pgraalvm
```

This will build the GraalVM native image inside a docker container and generate the `function.zip` ready for the deployment.


## Feature aws-lambda documentation

- [Micronaut AWS Lambda Function documentation](https://micronaut-projects.github.io/micronaut-aws/latest/guide/index.html#lambda)


## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)


## Feature aws-v2-sdk documentation

- [Micronaut AWS SDK 2.x documentation](https://micronaut-projects.github.io/micronaut-aws/latest/guide/)

- [https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/welcome.html](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/welcome.html)


## Feature dynamodb documentation

- [Micronaut Amazon DynamoDB documentation](https://micronaut-projects.github.io/micronaut-aws/latest/guide/#dynamodb)
- [https://aws.amazon.com/dynamodb/](https://aws.amazon.com/dynamodb/)

**<u>1-Index test DB structure</u>**

<img src="https://raw.githubusercontent.com/net2340445/MIT/main/193414.png" alt="db1" style="zoom: 33%;" /> 

**<u>2-BatchgetItem  and normal getItem test DB structure</u>**

<img src="https://raw.githubusercontent.com/net2340445/MIT/main/193753.png" alt="db2" style="zoom: 33%;" /> 


## Feature aws-lambda-custom-runtime documentation

- [Micronaut Custom AWS Lambda runtime documentation](https://micronaut-projects.github.io/micronaut-aws/latest/guide/index.html#lambdaCustomRuntimes)

- [https://docs.aws.amazon.com/lambda/latest/dg/runtimes-custom.html](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-custom.html)

## Java excute sample

```java
package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

public class Main {
    static HttpURLConnection connection;
    HttpHeaders headers;
    static URL url;
    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalAccessException {
        url = new URL("https://wqxboppwxc.execute-api.ap-northeast-1.amazonaws.com/dev/test");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        String authHeaderValue="eyJraWQiOiI5S0RUdDJuRmMxRktwRlpqOW1sYlpuQlF2MlZxa0JUbU16a0xoKys3dGhVPSIsImFsZyI6IlJTMjU2In0.eyJhdF9oYXNoIjoidmNnV09hNmdkMV82Z1NtVnBvT0g4QSIsInN1YiI6ImRmYTQ0ZTQ4LWUzYzgtNDg0Mi1hMGRhLTk5NGZmM2UyOWQzNiIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb21cL3VzLWVhc3QtMV96TVJRWVdtZVIiLCJjb2duaXRvOnVzZXJuYW1lIjoiZG9uZG9uIiwiYXVkIjoiNWp1ZG91Z2kzbmptbXJmN2c3Z3I2b2dpN2EiLCJ0b2tlbl91c2UiOiJpZCIsImF1dGhfdGltZSI6MTY1OTM0ODczMSwiZXhwIjoxNjU5NDM1MTMxLCJpYXQiOjE2NTkzNDg3MzEsImp0aSI6IjM4YmNkNGJiLTQ4MTgtNGViZC1iZTliLWI3ZjdkYzc4MThiZiIsImVtYWlsIjoibXVydWdhbnJhbTE5ODVAZ21haWwuY29tIn0.o7s7Yksp0RgOkDdTDCz5Nm_bJ-pTv-EcE26C4noc4ufqwZ151Jn-s0rFpBX757tKKqV7rjlIPg4nJ6vsuoblOjcuCAvYnRcCJT6M_Bhby1krVt8jVxWJKrTZI_DqMJ_3ON5Fyv1F4n1aPkqbXYKSUs2G2rCjO1_Ks-RRbvj4luzYX3KkO-QGn2xKU5atoMu2-VOVq28Bm4rGFKedXateRwYMsaxnpuIZcb6E_dH5Oa-XY0jfnnb2l5I6ZokGsBpMZSZbucSKhMI1cNn537M7vludwLbyBRFBUIRFJnbD4zuAYOiUoVdi0Ow1K_lo8auQHIWrpJuuYF-GLCgfc7l86A";
        connection.setRequestProperty("Authorization",authHeaderValue);
        connection.setRequestProperty("Content-Type","text/plain");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setDoOutput(true);
        String jsonInputString = "\"{'STORE_CODE':{'STORE1':'88888'},'JAN_CODE':{'JAN1':'4549509299998'},'MEMBER_RANK':'0','MEMBER_CODE':'','POINT_ALL':''}\"";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = " ";
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(connection.getRequestProperty("Authorization"));
            System.out.println(connection.getRequestMethod());
            System.out.println(connection.getResponseMessage());
            System.out.println(connection.getContentType());
            System.out.println(jsonInputString);
            System.out.println("Response body:" +response.toString());
            System.out.println(url);
            Map<String, List<String>> m1 = connection.getHeaderFields();
            System.out.println("Printing all response from URL"+url.toString());
            for(Map.Entry <String,List<String>> entry:m1.entrySet())
            {
                System.out.println(entry.getKey()+":"+entry.getValue());
            }

            List<String> contentLength = m1.get("Content-Length");
            if (contentLength == null) {
                System.out.println("'Content-Length' doesn't present in Header!");
            } else {
                for (String header : contentLength) {
                    System.out.println("Content-Length: " + header);
                }
            }
        }
    }
}
```

## Thanks

[Micronaut Framework (github.com)](https://github.com/micronaut-projects),[Oracle (github.com)](https://github.com/oracle)

## Copyright and license

Code and documentation Copyright (c) 2022 jiuzhou.chow. 

Code released under the [point-award-calculation/LICENSE at feature/pac-app-mvn · cainz-technology/point-award-calculation (github.com)](https://github.com/cainz-technology/point-award-calculation/blob/feature/pac-app-mvn/pac-app-mvn/LICENSE).

Enjoy 🤘
