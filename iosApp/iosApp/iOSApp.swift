import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinHelperKt.doInitKoin()
        NetworkUtilsKt.initializeNetworkUtils()
    }
    
    @StateObject private var navigationManager = NavigationManager()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(navigationManager)
        }
    }
}

extension View {
    func apply<V: View>(@ViewBuilder _ block: (Self) -> V) -> V { block(self) }
    
    @ViewBuilder
    func glassCompatibleButtonStyle() -> some View {
        if #available(iOS 26, *) {
            self.buttonStyle(.glassProminent)
        } else {
            self
        }
    }
}
