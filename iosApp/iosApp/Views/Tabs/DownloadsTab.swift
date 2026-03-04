//
//  DownloadsTab.swift
//  iosApp
//

import SwiftUI
import Shared

struct DownloadsTab: View {

    @ObservedObject private var viewModel = DownloadQueueViewModelS()

    @State private var deleteTarget: DownloadItem? = nil
    @State private var showDeleteConfirm: Bool = false

    private var speedSubtitle: String? {
        guard !viewModel.queueItems.isEmpty else { return nil }
        let down = ByteCountFormatter.string(fromByteCount: viewModel.downloadSpeed, countStyle: .binary)
        let up = ByteCountFormatter.string(fromByteCount: viewModel.uploadSpeed, countStyle: .binary)
        return "↓ \(down)/s  ↑ \(up)/s"
    }

    var body: some View {
        VStack(spacing: 0) {
            if let subtitle = speedSubtitle {
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 18)
                    .padding(.top, 8)
            }

            queueContent
        }
        .navigationTitle(MR.strings().downloads.localized())
        .onChange(of: viewModel.isCommandSuccess) { _, isSuccess in
            if isSuccess {
                deleteTarget = nil
                viewModel.resetCommandState()
            }
        }
        .confirmationDialog(
            MR.strings().delete_files.localized(),
            isPresented: $showDeleteConfirm,
            presenting: deleteTarget
        ) { item in
            Button(MR.strings().yes.localized(), role: .destructive) {
                viewModel.deleteDownload(item.id, deleteFiles: true)
            }
            Button(MR.strings().no.localized()) {
                viewModel.deleteDownload(item.id, deleteFiles: false)
            }
            Button(MR.strings().cancel.localized(), role: .cancel) {
                deleteTarget = nil
            }
        } message: { item in
            Text(item.name)
        }
    }

    @ViewBuilder
    private var queueContent: some View {
        if viewModel.isLoading && viewModel.queueItems.isEmpty {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if viewModel.queueItems.isEmpty {
            emptyView
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ScrollView {
                VStack(spacing: 12) {
                    ForEach(viewModel.queueItems, id: \.id) { item in
                        DownloadQueueItemView(
                            item: item,
                            actionInProgress: viewModel.isCommandLoading,
                            onPause: { viewModel.pauseDownload(item.id) },
                            onResume: { viewModel.resumeDownload(item.id) },
                            onDelete: {
                                deleteTarget = item
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
                .padding(.vertical, 12)
                .padding(.horizontal, 18)
            }
        }
    }

    @ViewBuilder
    private var emptyView: some View {
        VStack(alignment: .center, spacing: 12) {
            Image(systemName: "arrow.down.circle")
                .font(.system(size: 64))
                .foregroundStyle(.secondary)
            Text(MR.strings().no_activity.localized())
                .font(.system(size: 20, weight: .bold))
        }
        .padding(.horizontal, 24)
    }
}
