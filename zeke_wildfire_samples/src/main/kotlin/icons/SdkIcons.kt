package icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon


class SdkIcons {
    companion object{
        val Sdk_default_icon: Icon = IconLoader.getIcon("/icons/sdk_16.svg", SdkIcons::class.java)
    }
}
