//
//  DownloadQueueViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class DownloadQueueViewModelS: ObservableObject {
    private let viewModel: DownloadQueueViewModel

    @Published private(set) var queueState: DownloadQueueState = DownloadQueueStateInitial()
    @Published private(set) var queueItems: [DownloadItem] = []
    @Published private(set) var downloadSpeed: Int64 = 0
    @Published private(set) var uploadSpeed: Int64 = 0
    @Published private(set) var isLoading: Bool = false
    @Published private(set) var commandState: DownloadClientCommandState = DownloadClientCommandStateInitial()
    @Published private(set) var isCommandLoading: Bool = false
    @Published private(set) var isCommandSuccess: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getDownloadQueueViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.downloadQueueState.observeAsync { state in
            self.queueState = state
            self.isLoading = state is DownloadQueueStateLoading
            if let success = state as? DownloadQueueStateSuccess {
                self.queueItems = success.items
                self.downloadSpeed = success.transferInfo.downloadSpeed
                self.uploadSpeed = success.transferInfo.uploadSpeed
            }
        }
        viewModel.commandState.observeAsync { state in
            self.commandState = state
            self.isCommandLoading = state is DownloadClientCommandStateLoading
            self.isCommandSuccess = state is DownloadClientCommandStateSuccess
        }
    }

    func pauseDownload(_ id: String) {
        viewModel.pauseDownload(id: id)
    }

    func resumeDownload(_ id: String) {
        viewModel.resumeDownload(id: id)
    }

    func deleteDownload(_ id: String, deleteFiles: Bool) {
        viewModel.deleteDownload(id: id, deleteFiles: deleteFiles)
    }

    func resetCommandState() {
        viewModel.resetCommandState()
    }
}
