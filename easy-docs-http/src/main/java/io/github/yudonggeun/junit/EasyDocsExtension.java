package io.github.yudonggeun.junit;

import io.github.yudonggeun.Document;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EasyDocsExtension implements AfterAllCallback {

    @Override
    public void afterAll(ExtensionContext context) {
        String openApi3 = Document.getOpenApi3(4);
        saveToResourceDirectory("openapi3.json", openApi3);
    }

    private void saveToResourceDirectory(String filename, String content) {
        try {
            String projectDir = System.getProperty("user.dir");
            String outputDir = projectDir + "/build/generated/docs/";
            File file = new File(outputDir + filename);
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }
            System.out.println("OpenAPI 문서화 파일이 저장되었습니다:" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
