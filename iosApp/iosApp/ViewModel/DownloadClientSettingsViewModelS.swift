//
//  DownloadClientSettingsViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class DownloadClientSettingsViewModelS: ObservableObject {
    private let viewModel: DownloadClientSettingsViewModel

    @Published private(set) var downloadClients: [DownloadClient] = []
    @Published private(set) var connectionStates: [KotlinLong: DownloadClientConnectionState] = [:]
    @Published private(set) var mutationState: DownloadClientMutationState = DownloadClientMutationStateInitial()
    @Published private(set) var isMutationSuccess: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getDownloadClientSettingsViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.downloadClients.observeAsync { self.downloadClients = $0 }
        viewModel.connectionStates.observeAsync { self.connectionStates = $0 }
        viewModel.mutationState.observeAsync { state in
            self.mutationState = state
            self.isMutationSuccess = state is DownloadClientMutationStateSuccess
        }
    }

    func testConnection(id: Int64) {
        viewModel.testConnection(id: id)
    }

    func deleteClient(_ client: DownloadClient) {
        viewModel.deleteClient(downloadClient: client)
    }

    func resetMutationState() {
        viewModel.resetMutationState()
    }
}
