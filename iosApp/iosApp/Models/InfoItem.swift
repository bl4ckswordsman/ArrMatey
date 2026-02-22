//
//  InfoItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-22.
//

struct InfoItem: Hashable, Identifiable {
    let label: String
    let value: String
    
    var id: String { "\(label)-\(value)" }
}
