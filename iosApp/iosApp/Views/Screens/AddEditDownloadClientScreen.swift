//
//  AddEditDownloadClientScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct AddEditDownloadClientScreen: View {

    @ObservedObject private var viewModel: DownloadClientSettingsViewModelS
    @Environment(\.dismiss) private var dismiss

    let existingClient: DownloadClient?

    @State private var label: String
    @State private var selectedType: DownloadClientType
    @State private var url: String
    @State private var username: String
    @State private var password: String
    @State private var apiKey: String
    @State private var enabled: Bool
    @State private var errorMessage: String? = nil

    private var isEditing: Bool { existingClient != nil }

    init(viewModel: DownloadClientSettingsViewModelS, client: DownloadClient? = nil) {
        self._viewModel = ObservedObject(wrappedValue: viewModel)
        self.existingClient = client
        _label = State(initialValue: client?.label ?? "")
        _selectedType = State(initialValue: client?.type ?? .qBittorrent)
        _url = State(initialValue: client?.url ?? "")
        _username = State(initialValue: client?.username ?? "")
        _password = State(initialValue: client?.password ?? "")
        _apiKey = State(initialValue: client?.apiKey ?? "")
        _enabled = State(initialValue: client?.enabled ?? true)
    }

    var body: some View {
        Form {
            Section {
                TextField(MR.strings().client_label.localized(), text: $label)
                Picker(MR.strings().client_type.localized(), selection: $selectedType) {
                    ForEach(DownloadClientType.entries, id: \.self) { type in
                        Text(type.displayName).tag(type)
                    }
                }
                TextField(MR.strings().client_url.localized(), text: $url)
                    .keyboardType(.URL)
                    .autocapitalization(.none)
                TextField(MR.strings().client_username.localized(), text: $username)
                    .autocapitalization(.none)
                SecureField(MR.strings().client_password.localized(), text: $password)
                TextField(MR.strings().client_api_key.localized(), text: $apiKey)
                    .autocapitalization(.none)
                Toggle(MR.strings().client_enabled.localized(), isOn: $enabled)
            }

            if let errorMessage {
                Section {
                    Text(errorMessage)
                        .foregroundStyle(.red)
                }
            }

            Section {
                Button(MR.strings().save.localized()) {
                    saveClient()
                }
            }
        }
        .navigationTitle(isEditing ? MR.strings().edit_download_client.localized() : MR.strings().add_download_client.localized())
        .onChange(of: viewModel.mutationState) { _, state in
            if state is DownloadClientMutationStateSuccess {
                viewModel.resetMutationState()
                dismiss()
            } else if let conflict = state as? DownloadClientMutationStateConflict {
                errorMessage = MR.strings().conflict_on_fields.localized()
            } else if let error = state as? DownloadClientMutationStateError {
                errorMessage = error.message
            } else {
                errorMessage = nil
            }
        }
        .onAppear {
            viewModel.resetMutationState()
        }
    }

    private func saveClient() {
        let client = DownloadClient(
            id: existingClient?.id ?? 0,
            type: selectedType,
            label: label,
            url: url,
            username: username,
            password: password,
            apiKey: apiKey,
            enabled: enabled,
            selected: existingClient?.selected ?? false
        )
        if isEditing {
            viewModel.updateClient(client)
        } else {
            viewModel.createClient(client)
        }
    }
}
