//
//  DownloadQueueItemView.swift
//  iosApp
//

import SwiftUI
import Shared

struct DownloadQueueItemView: View {
    let item: DownloadItem
    let actionInProgress: Bool
    let onPause: () -> Void
    let onResume: () -> Void
    let onDelete: () -> Void

    private var statusLabel: String {
        switch item.status {
        case .downloading: return "Downloading"
        case .paused:      return "Paused"
        case .queued:      return "Queued"
        case .completed:   return "Completed"
        case .failed:      return "Failed"
        case .seeding:     return "Seeding"
        case .stalled:     return "Stalled"
        default:           return item.status.name
        }
    }

    private var canPause: Bool { item.status == .downloading || item.status == .seeding }
    private var canResume: Bool { item.status == .paused || item.status == .stalled }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.name)
                        .font(.body)
                        .fontWeight(.medium)
                        .lineLimit(2)
                    Text(statusLabel)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                HStack(spacing: 4) {
                    if canPause {
                        Button(action: onPause) {
                            Image(systemName: "pause.fill")
                        }
                        .disabled(actionInProgress)
                    } else if canResume {
                        Button(action: onResume) {
                            Image(systemName: "play.fill")
                        }
                        .disabled(actionInProgress)
                    }
                    Button(role: .destructive, action: onDelete) {
                        Image(systemName: "trash")
                    }
                    .disabled(actionInProgress)
                }
                .buttonStyle(.bordered)
                .controlSize(.small)
            }

            ProgressView(value: item.progress)
                .tint(progressColor)

            if !item.eta.isEmpty {
                Text(item.eta)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(12)
        .background(Color(uiColor: .secondarySystemBackground))
        .cornerRadius(12)
    }

    private var progressColor: Color {
        switch item.status {
        case .failed:  return .red
        case .stalled: return .orange
        case .seeding: return .green
        default:       return .accentColor
        }
    }
}
