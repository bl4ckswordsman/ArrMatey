//
//  DownloadClientScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct DownloadClientScreen: View {

    @ObservedObject private var viewModel = DownloadClientSettingsViewModelS()
    @State private var deleteTarget: DownloadClient? = nil

    var body: some View {
        Form {
            if viewModel.downloadClients.isEmpty {
                Text(MR.strings().no_download_clients.localized())
                    .foregroundStyle(.secondary)
            } else {
                Section {
                    ForEach(viewModel.downloadClients, id: \.id) { client in
                        DownloadClientRow(
                            client: client,
                            connectionState: viewModel.connectionStates[client.id.asKotlinLong],
                            onTest: { viewModel.testConnection(id: client.id) },
                            onDelete: { deleteTarget = client }
                        )
                    }
                }
            }
        }
        .navigationTitle(MR.strings().download_clients.localized())
        .onChange(of: viewModel.isMutationSuccess) { _, isSuccess in
            if isSuccess {
                deleteTarget = nil
                viewModel.resetMutationState()
            }
        }
        .alert(
            MR.strings().confirm.localized(),
            isPresented: Binding(
                get: { deleteTarget != nil },
                set: { if !$0 { deleteTarget = nil } }
            ),
            presenting: deleteTarget
        ) { client in
            Button(MR.strings().yes.localized(), role: .destructive) {
                viewModel.deleteClient(client)
            }
            Button(MR.strings().cancel.localized(), role: .cancel) {
                deleteTarget = nil
            }
        } message: { client in
            Text(MR.strings().confirm_delete_instance.formatted(args: [client.label]))
        }
    }
}

private struct DownloadClientRow: View {
    let client: DownloadClient
    let connectionState: DownloadClientConnectionState?
    let onTest: () -> Void
    let onDelete: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 16) {
                SVGImageView(filename: client.type.iconKey)
                    .frame(width: 32, height: 32)
                VStack(alignment: .leading, spacing: 2) {
                    Text(client.label)
                        .font(.system(size: 17, weight: .medium))
                    Text(client.url)
                        .font(.system(size: 14))
                        .foregroundStyle(.secondary)
                }
                Spacer()
                connectionIndicator
            }

            HStack {
                Button(MR.strings().test_connection.localized(), action: onTest)
                    .disabled(connectionState is DownloadClientConnectionStateLoading)
                    .buttonStyle(.bordered)
                    .controlSize(.small)

                Spacer()

                Button(role: .destructive, action: onDelete) {
                    Image(systemName: "trash")
                }
                .buttonStyle(.bordered)
                .controlSize(.small)
            }
        }
        .padding(.vertical, 4)
    }

    @ViewBuilder
    private var connectionIndicator: some View {
        Group {
            switch connectionState {
            case is DownloadClientConnectionStateLoading:
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle())
            case is DownloadClientConnectionStateSuccess:
                Image(systemName: "checkmark.circle.fill")
                    .foregroundStyle(.green)
            case is DownloadClientConnectionStateError:
                Image(systemName: "wifi.slash")
                    .foregroundStyle(.red)
            default:
                Image(systemName: "circle")
                    .foregroundStyle(.secondary)
            }
        }
        .frame(width: 20, height: 20)
    }
}
