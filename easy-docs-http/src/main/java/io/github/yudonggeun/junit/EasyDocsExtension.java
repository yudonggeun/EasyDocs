package io.github.yudonggeun.junit;

import io.github.yudonggeun.Document;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EasyDocsExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        String openApi3 = Document.getOpenApi3(4);
        System.out.println(openApi3);
    }
}
