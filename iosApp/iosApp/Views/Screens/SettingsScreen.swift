//
//  SettingsScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-11.
//

import SwiftUI
import Shared

struct SettingsScreen: View {
    
    @Environment(\.openURL) private var openURL
    @EnvironmentObject private var navigationManager: NavigationManager
    
    @ObservedObject private var viewModel = MoreScreenViewModelS()
    
    @State private var showLibrariesSheet: Bool = false
    
    private var instances: [Instance] {
        viewModel.instances
    }
    
    private func route(for instance: Instance) -> SettingsRoute {
        switch instance.type {
        case .sonarr, .radarr, .lidarr:
            return .arrDashboard(instance.id)
        default:
            return .editInstance(instance.id)
        }
    }
    
    var body: some View {
        Form {
            Section {
                ForEach(instances, id: \.self) { instance in
                    NavigationLink(value: route(for: instance)) {
                        HStack(spacing: 24){
                            SVGImageView(filename: instance.type.iconKey)
                                .frame(width: 32, height: 32)
                            VStack(alignment: .leading, spacing: 1) {
                                HStack(alignment: .center, spacing: 12) {
                                    Text(instance.label)
                                        .font(.system(size: 18, weight: .medium))
                                    Group {
                                        switch viewModel.connectionStatuses[instance.id.asKotlinLong] {
                                        case is OperationStatusInProgress:
                                            ProgressView()
                                                .progressViewStyle(CircularProgressViewStyle())
                                        case is OperationStatusError:
                                            Image(systemName: "wifi.slash")
                                                .tint(.red)
                                        case is OperationStatusSuccess:
                                            Image(systemName: "wifi")
                                        default: ZStack{}
                                        }
                                    }
                                    .frame(width: 8, height: 8)
                                }
                                Text(instance.url)
                                    .font(.system(size: 16))
                            }
                        }
                    }
                }
                NavigationLink(value: SettingsRoute.newInstance()) {
                    Text(MR.strings().add_instance.localized())
                        .foregroundColor(.themePrimary)
                }
            } header: {
                Text(MR.strings().instances.localized())
            }
            
            Section {
                NavigationLink(value: SettingsRoute.navigationConfig) {
                    HStack(spacing: 24) {
                        Image(systemName: "location.north.fill")
                            .foregroundColor(.themePrimary)
                            .frame(width: 32, height: 32)
                        
                        VStack(alignment: .leading, spacing: 1) {
                            Text(MR.strings().navigation_bar_configuration.localized())
                                .font(.system(size: 18, weight: .medium))
                        }
                    }
                }
            }
            
            AboutCard(
                onBugReportClick: { if let url = URL(string: MR.strings().bug_report_link.localized()) {
                    openURL(url)
                } },
                onGitHubClick: { if let url = URL(string: MR.strings().app_link.localized()) {
                    openURL(url)
                } },
                onDonateClick: { if let url = URL(string: MR.strings().bmac_link.localized()) {
                    openURL(url)
                } },
                onLibrariesClick: { showLibrariesSheet = true }
            )
            
            Section {
                if isDebug() {
                    Button("Dev settings") {
                        navigationManager.go(to: .dev)
                    }
                }
            }
        }
        .navigationTitle(MR.strings().settings.localized())
        .sheet(isPresented: $showLibrariesSheet) {
            LibrariesSheet()
        }
    }
}
