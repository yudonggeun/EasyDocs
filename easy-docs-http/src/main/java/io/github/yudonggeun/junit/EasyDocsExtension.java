package io.github.yudonggeun.junit;

import io.github.yudonggeun.Document;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EasyDocsExtension implements AfterAllCallback {

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        String openApi3 = Document.getOpenApi3(4);
        System.out.println(openApi3);
    }
}
