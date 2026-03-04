//
//  ProwlarrIndexersView.swift
//  iosApp
//
//  Created by Bryan Moon on 2026-03-04.
//

import SwiftUI
import Shared

struct ProwlarrIndexersView: View {
    @ObservedObject private var viewModel = ProwlarrIndexersViewModelS()
    
    var body: some View {
        VStack(spacing: 0) {
            indexersContent
        }
    }
    
    @ViewBuilder
    private var indexersContent: some View {
        if viewModel.indexers is IndexersStateLoading {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if viewModel.indexers is IndexersStateError {
            if let error = viewModel.indexers as? IndexersStateError {
                errorView(message: error.message)
            }
        } else if viewModel.indexers is IndexersStateSuccess {
            if let success = viewModel.indexers as? IndexersStateSuccess {
                if success.items.isEmpty {
                    emptyView
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        VStack(spacing: 12) {
                            ForEach(Array(success.items.enumerated()), id: \.element.id) { _, indexer in
                                IndexerRow(indexer: indexer)
                            }
                        }
                        .padding(.vertical, 12)
                        .padding(.horizontal, 16)
                    }
                }
            }
        } else {
            // Initial state
            initialView
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
    
    @ViewBuilder
    private var emptyView: some View {
        VStack(spacing: 12) {
            Image(systemName: "list.bullet.rectangle")
                .font(.system(size: 64))
                .foregroundStyle(.secondary)
            Text("No indexers configured")
                .font(.system(size: 17))
                .foregroundStyle(.secondary)
        }
    }
    
    @ViewBuilder
    private var initialView: some View {
        VStack(spacing: 12) {
            ProgressView()
            Text("Loading indexers...")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }
    
    private func errorView(message: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundStyle(.red)
            Text(message)
                .font(.system(size: 15))
                .foregroundStyle(.red)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct IndexerRow: View {
    let indexer: ProwlarrIndexer
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(indexer.name ?? indexer.implementationName ?? "Unknown")
                    .font(.system(size: 16, weight: .semibold))
                    .lineLimit(1)
                
                Spacer()
                
                // Protocol badge
                let protocolText = (indexer.protocol as? String) ?? "Unknown"
                Text(protocolText.capitalized)
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(protocolColor(for: protocolText))
                    .foregroundColor(.white)
                    .cornerRadius(4)
            }
            
            HStack(spacing: 12) {
                // Enable status
                HStack(spacing: 4) {
                    Circle()
                        .fill(indexer.enable ? Color.green : Color.gray)
                        .frame(width: 8, height: 8)
                    Text(indexer.enable ? "Enabled" : "Disabled")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                
                // RSS support
                if indexer.supportsRss {
                    HStack(spacing: 2) {
                        Image(systemName: "dot.radiowaves.left.and.right")
                            .font(.caption)
                        Text("RSS")
                            .font(.caption)
                    }
                    .foregroundStyle(.secondary)
                }
                
                // Search support
                if indexer.supportsSearch {
                    HStack(spacing: 2) {
                        Image(systemName: "magnifyingglass")
                            .font(.caption)
                        Text(MR.strings().search.localized())
                            .font(.caption)
                    }
                    .foregroundStyle(.secondary)
                }
            }
            
            if let message = indexer.message, !message.isEmpty {
                Text(message)
                    .font(.caption)
                    .foregroundStyle(.red)
                    .lineLimit(2)
            }
        }
        .padding(12)
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
    
    private func protocolColor(for proto: String) -> Color {
        switch proto.lowercased() {
        case "torrent": return .blue
        case "usenet": return .green
        default: return .gray
        }
    }
}
