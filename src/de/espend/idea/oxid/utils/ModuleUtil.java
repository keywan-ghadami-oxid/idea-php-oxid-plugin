package de.espend.idea.oxid.utils;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.smarty.SmartyFileType;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ModuleUtil {

    public static void visitModuleTemplatesInMetadataScope(@NotNull PsiFile metaFile, final @NotNull ModuleFileVisitor visitor) {

        ModuleUtil.visitModuleFile(metaFile, new ModuleUtil.ModuleFileVisitor() {
            @Override
            public void visit(@NotNull VirtualFile virtualFile, @NotNull String relativePath) {

                if (virtualFile.getFileType() != SmartyFileType.INSTANCE) {
                    return;
                }

                String[] split = relativePath.split("/");
                if(split.length <= 2) {
                    return;
                }

                int n = split.length - 2;
                String[] newArray = new String[n];
                System.arraycopy(split, 2, newArray, 0, n);

                visitor.visit(virtualFile, StringUtils.join(newArray, "/"));
            }
        });
    }

    public static void visitModuleFile(@NotNull PsiFile metaFile, final @NotNull ModuleFileVisitor visitor) {

        final VirtualFile moduleFolder = MetadataUtil.getModuleVendorFolderFromMetadata(metaFile);
        if(moduleFolder == null) {
            return;
        }

        PsiDirectory parent = metaFile.getParent();
        if(parent == null) {
            return;
        }

        VfsUtil.visitChildrenRecursively(parent.getVirtualFile(), new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {

                if (file.isDirectory()) {
                    return super.visitFile(file);
                }

                String relativePath = VfsUtil.getRelativePath(file, moduleFolder.getParent(), '/');
                if(relativePath == null) {
                    return super.visitFile(file);
                }

                visitor.visit(file, relativePath);

                return super.visitFile(file);
            }
        });
    }

    public interface ModuleFileVisitor {
        void visit(@NotNull VirtualFile virtualFile, @NotNull String relativePath);
    }

}